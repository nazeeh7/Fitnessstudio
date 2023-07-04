package kickstart.mitglieder;

import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.salespointframework.useraccount.Password;
import org.salespointframework.useraccount.Role;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;

import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.MediaType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import com.itextpdf.layout.element.Paragraph;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.mail.SimpleMailMessage;
import java.util.Map;
import java.util.HashMap;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MemberController {

	@Autowired
	private MemberRepository memberRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private TrainingPlanRepository trainingPlanRepository;

	@Autowired
	private MembershipContractRepository contractRepository;

	@Autowired
	private MembershipContractRepository membershipContractRepository;

	@Autowired
	private MemberService memberService;

	@Autowired
	private UserAccountManagement userAccountManager;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private CancellationRequestRepository cancellationRequestRepository;

	
	
	
	// @PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/members")
	public String viewHomePage(Model model) {
		model.addAttribute("members", memberRepository.findAll());
		return "members";
	}

	@GetMapping("/newMemberForm")
	public String showNewMemberForm(Model model) {
		Member member = new Member();
		model.addAttribute("member", member);
		return "new_member";
	}

	@PostMapping("/saveMember")
	public String saveMember(@ModelAttribute("member") Member member,
	                         @RequestParam String username,
	                         @RequestParam String password,
	                         Model model) {

	    // Überprüfen Sie, ob der Benutzername bereits existiert
		 Optional<Member> existingMember = memberRepository.findByUsername(username);
		    if (existingMember.isPresent() || username.equalsIgnoreCase("boss")) {
		        model.addAttribute("error", "Der Benutzername ist bereits vergeben oder unzulässig.");
		        return "new_member";
		    }

	    // Benutzerkonto erstellen und speichern
	    UserAccount userAccount = userAccountManager.create(username, Password.UnencryptedPassword.of(password),
	            Role.of("USER"));
	    userAccountManager.save(userAccount);

	    member.setRole("USER");

	    // Hier erhalten Sie die Contract-Instanz und setzen Sie die contract_id
	    MembershipContract contract = member.getContract();
	    member.setContract(contract);

	    // Speichern Sie das Mitglied erst, nachdem Sie überprüft haben, ob der Benutzername bereits existiert und das Benutzerkonto erstellt wurde
	    memberRepository.save(member);

	    return "redirect:/members";
	}



	@PostMapping("/updateMember")
	public String updateMember(@ModelAttribute("member") Member member) {

	    Optional<Member> existingMemberOpt = memberRepository.findById(member.getId());

	    if (existingMemberOpt.isPresent()) {
	        Member existingMember = existingMemberOpt.get();

	        existingMember.setFirstName(member.getFirstName());
	        existingMember.setLastName(member.getLastName());
	        existingMember.setAddress(member.getAddress());
	        existingMember.setBirthdate(member.getBirthdate());
	        existingMember.setGender(member.getGender());
	        existingMember.setPhone(member.getPhone());
	        existingMember.setEmail(member.getEmail());

	        memberRepository.save(existingMember);
	    }

	    return "redirect:/members";
	}



	@Transactional
	@PostMapping("/delete_member/{id}")
	public String deleteMember(@PathVariable Long id) {
	    Optional<Member> memberOptional = memberRepository.findById(id);
	    if (memberOptional.isPresent()) {
	        Member member = memberOptional.get();
	        MembershipContract contract = member.getContract();

	        if (contract != null) {
	            member.setContract(null); // Remove the reference to the contract
	            memberRepository.save(member); // Save the member with the updated contract reference
	            cancellationRequestRepository.deleteByMember(member); // Remove the cancellation requests related to the member
	            membershipContractRepository.delete(contract);
	        }
	        // Löschen der zugehörigen Benachrichtigungen
	        List<Notification> notifications = notificationRepository.findAllByMemberId(member.getId());
	        notificationRepository.deleteAll(notifications);
	        
	        // Benutzerkonto des Mitglieds löschen
	        Optional<UserAccount> userAccountOptional = userAccountManager.findByUsername(member.getUsername());
	        userAccountOptional.ifPresent(userAccount -> userAccountManager.delete(userAccount));

	        // Dann das Mitglied löschen
	        trainingPlanRepository.deleteByMemberId(member.getId());
	        memberRepository.delete(member);
	    }

	    return "redirect:/members";
	}






	@GetMapping("/delete_member/{id}/confirm")
	public String confirmDeleteMember(@PathVariable Long id, Model model) {
		Optional<Member> member = memberRepository.findById(id);
		if (member.isPresent()) {
			model.addAttribute("member", member.get());
			return "delete_member";
		} else {
			return "error";
		}
	}

	@GetMapping("/edit_member/{id}")
	public String showEditMemberForm(@PathVariable Long id, Model model) {
		Optional<Member> member = memberRepository.findById(id);
		if (member.isPresent()) {
			model.addAttribute("member", member.get());
			return "edit_member";
		} else {
			return "error";
		}
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/member-dashboard")
	public String showMemberDashboard() {
		return "member-dashboard";
	}

	@GetMapping("/contracts")
	public String showContracts(Model model) {
	    List<MembershipContract> contracts = membershipContractRepository.findAll();
	    model.addAttribute("contracts", contracts);
	   // model.addAttribute("members", memberRepository.findAll());
	    return "contracts";
	}



	@PostMapping("/saveContract")
	public String saveContract(@ModelAttribute("contract") MembershipContract contract, @RequestParam Long memberId) {
		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid member Id:" + memberId));
		member.setContract(contract);
		contract.setMember(member);
		memberRepository.save(member);
		return "redirect:/contracts";
	}
	@PostMapping("/updateContract/{id}")
	public String updateContract(@PathVariable("id") Long id, @ModelAttribute("contract") MembershipContract updatedContract) {
	    MembershipContract existingContract = membershipContractRepository.findById(id)
	        .orElseThrow(() -> new IllegalArgumentException("Invalid contract Id:" + id));

	    // Jetzt aktualisieren wir das bestehende Vertragsobjekt mit den Werten des aktualisierten Vertrags.
	    existingContract.setPackageType(updatedContract.getPackageType());
	    existingContract.setPaymentDetails(updatedContract.getPaymentDetails());
	    existingContract.setContactDetails(updatedContract.getContactDetails());
	    existingContract.setStatus(updatedContract.getStatus());
	    existingContract.setStartDate(updatedContract.getStartDate());
	    existingContract.setEndDate(updatedContract.getEndDate());
	    existingContract.setPrice(updatedContract.getPrice());

	    // Speichern Sie das bestehende Vertragsobjekt, das nun aktualisiert wurde.
	    membershipContractRepository.save(existingContract);

	    return "redirect:/contracts";
	}

	@GetMapping("/addContract")
	public String showAddContractForm(Model model) {
	    model.addAttribute("contract", new MembershipContract());

	    Iterable<Member> iterable = memberRepository.findAll();
	    List<Member> members = StreamSupport.stream(iterable.spliterator(), false)
	        .filter(member -> member.getContract() == null)
	        .collect(Collectors.toList());

	    model.addAttribute("members", members);

	    return "add-contract";
	}

	@GetMapping("/newContractForm")
	public String showNewContractForm(Model model) {
	    MembershipContract contract = new MembershipContract();
	    model.addAttribute("contract", contract);

	    Iterable<Member> iterable = memberRepository.findAll();
	    List<Member> members = StreamSupport.stream(iterable.spliterator(), false)
	        .filter(member -> member.getContract() == null)
	        .collect(Collectors.toList());

	    model.addAttribute("members", members);

	    return "new_contract";
	}


	@GetMapping("/viewContract/{id}")
	public String viewContract(@PathVariable("id") Long id, Model model) {
		MembershipContract contract = membershipContractRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid contract Id:" + id));
		model.addAttribute("contract", contract);
		model.addAttribute("contractId", contract.getId()); // Setzen Sie die contractId im Model
		return "view-contract";
	}

	@GetMapping("/editContract/{id}")
	public String showEditContractForm(@PathVariable("id") Long id, Model model) {
	    Optional<MembershipContract> contractOptional = membershipContractRepository.findById(id);
	    if (contractOptional.isPresent()) {
	        MembershipContract contract = contractOptional.get();
	        model.addAttribute("contract", contract);
	        model.addAttribute("members", memberRepository.findAll());
	        return "edit-contract";
	    } else {
	        // handle case when contract is not found
	        return "redirect:/contracts";
	    }
	}


	@GetMapping("/deleteContract/{id}")
	public String deleteContract(@PathVariable("id") Long id) {
	    Optional<MembershipContract> contractOptional = membershipContractRepository.findById(id);
	    if (contractOptional.isPresent()) {
	        MembershipContract contract = contractOptional.get();
	        Member member = contract.getMember();
	        
	        // Löschen der zugehörigen Kündigungsanträge
	        List<CancellationRequest> cancellationRequests = cancellationRequestRepository.findByContract(contract);
	        cancellationRequestRepository.deleteAll(cancellationRequests);
	        
	        if (member != null && contract != null) {
	            member.setContract(null); // Remove the reference to the contract only if member and contract are not null
	            memberRepository.save(member); // Save the member with the updated contract reference
	        }
	        
	        membershipContractRepository.delete(contract); // Now delete the contract only if it is not null
	        
	        return "redirect:/contracts";
	    } else {
	        // handle case when contract is not found
	        return "redirect:/contracts";
	    }
	}





	@GetMapping("/contract/{contractId}/download")
	public ResponseEntity<ByteArrayResource> downloadContractDetails(@PathVariable String contractId)
			throws IOException {
		Long contractIdLong = Long.parseLong(contractId);
		Optional<MembershipContract> contractOptional = contractRepository.findById(contractIdLong);
		if (!contractOptional.isPresent()) {
			return ResponseEntity.notFound().build();
		}
		MembershipContract contract = contractOptional.get();

		if (contract == null) {
			return ResponseEntity.notFound().build();
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfWriter writer = new PdfWriter(outputStream);
		PdfDocument pdf = new PdfDocument(writer);
		Document document = new Document(pdf);

		Paragraph topLeftText = new Paragraph("Fittnessstudio").setFontSize(12).setMarginTop(10).setMarginLeft(20);

		document.add(topLeftText);

		// Add the Title
		Paragraph title = new Paragraph("Rechnung").setTextAlignment(TextAlignment.CENTER).setFontSize(20);
		document.add(title);

		Paragraph topRightText = new Paragraph(LocalDate.now().toString()).setFontSize(12).setMarginTop(10)
				.setMarginRight(20).setTextAlignment(TextAlignment.RIGHT);

		document.add(topRightText);

		// Add the Text below the title (in Englisch)
		Paragraph englischerText = new Paragraph("Sehr geehrte Damen und Herren,\nhier ist Ihre Rechnung").setFontSize(12)
				.setMarginTop(20).setMarginBottom(20);

		document.add(englischerText);

		// Add some space before the table
		document.add(new Paragraph("\n\n"));

		Table table = new Table(8); // Create a table with eight columns.
		table.addCell("ID");
		table.addCell("Mitglied");
		table.addCell("Pakettyp");
		table.addCell("Zahlungsdetails");
		table.addCell("Preis");
		table.addCell("Startdatum");
		table.addCell("Enddatum");
		table.addCell("Status");

		table.addCell(contract.getId().toString());
		table.addCell(contract.getMember() != null
				? contract.getMember().getFirstName() + ' ' + contract.getMember().getLastName()
				: "");
		table.addCell(contract.getPackageType());
		table.addCell(contract.getPaymentDetails());
		table.addCell(Double.toString(contract.getPrice()) + "€");
		table.addCell(contract.getStartDate() != null ? contract.getStartDate().toString() : "Nicht festgelegt");
		table.addCell(contract.getEndDate() != null ? contract.getEndDate().toString() : "Nicht festgelegt");
		table.addCell(contract.getStatus());
		// Calculate the monthly fee
		double monthlyFee = contract.getPrice() / 12;  // Just an example, calculate the monthly fee based on your business rules

		// Add the monthly fee to the table
		table.addCell("");  // Empty ID cell
		table.addCell("");  // Empty Member cell
		table.addCell("");  // Empty Package Type cell
		table.addCell("");  // Empty Payment Details cell
		table.addCell(String.format("%.2f€", monthlyFee));  // Monthly fee
		table.addCell("");  // Empty Start Date cell
		table.addCell("");  // Empty End Date cell
		table.addCell("Monatsbeitrag");  // Status cell
		document.add(table);

		document.close();
		byte[] bytes = outputStream.toByteArray();

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rechnung.pdf");

		ByteArrayResource resource = new ByteArrayResource(bytes);

		return ResponseEntity.ok().headers(headers).contentLength(bytes.length)
				.contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
	}

	@GetMapping("/ansicht_member_dashboard")
	public String showAnsichtMemberDashboard(Model model, Principal principal) {
	    // Holen Sie den Benutzernamen des aktuellen Benutzers
	    String username = principal.getName();

	    // Suchen Sie das Mitglied mit diesem Benutzernamen
	    Optional<Member> memberOptional = memberService.getMemberByUsername(username);

	    if (memberOptional.isPresent()) {
	        Member member = memberOptional.get();

	        // Holen Sie den Trainingsplan des Mitglieds
	        Optional<TrainingPlan> trainingPlanOptional = trainingPlanRepository.findByMember(member);

	        if (trainingPlanOptional.isPresent()) {
	            // Fügen Sie die ID des Trainingsplans zum Modell hinzu
	        	 model.addAttribute("trainingPlan", trainingPlanOptional.get());
	        }
	        // Überprüfen Sie den Zustand des Kündigungsantrags
	        List<CancellationRequest> cancellationRequests = cancellationRequestRepository.findByMemberAndApproval(member, "Genehmigt");
	        boolean isCancellationApproved = false;

	        for (CancellationRequest cancellationRequest : cancellationRequests) {
	            System.out.println("Genehmigungsstatus: " + cancellationRequest.getApproval());
	            if (cancellationRequest.getApproval().equals("Genehmigt")) {
	                isCancellationApproved = true;
	                break;
	            }
	            else {
	            	   System.out.println("Genehmigungsstatus: " + cancellationRequest.getApproval());
	            }
	        }

	        if (isCancellationApproved) {
	            model.addAttribute("message", "Kündigungsantrag genehmigt");
	        } else {
	            model.addAttribute("message", "");
	        }


	    }
	    return "ansicht_member_dashboard";
	}
	
	
	@GetMapping("/trainingPlan/{id}")
	public String showTrainingPlanView(@PathVariable("id") Long id, Model model) {
	    Optional<TrainingPlan> trainingPlanOptional = trainingPlanRepository.findById(id);
	    System.out.println("Training Plan Optional: " + trainingPlanOptional); // debug information
	    if (trainingPlanOptional.isPresent() && trainingPlanOptional.get().getMember() != null) {
	        model.addAttribute("trainingPlan", trainingPlanOptional.get());
	    } else {
	        model.addAttribute("trainingPlan", null);
	    }
	    return "ansicht-training-plan-view";
	}

//
	@GetMapping("/trainingPlan")
	public String showTrainingPlanView1(Model model) {
	    model.addAttribute("trainingPlan", null);
	    return "ansicht-training-plan-view";
	}



	// @PreAuthorize("hasRole('USER')")
	@GetMapping("/member_personal_infos")
	public String showPersonalInfos(Model model, Principal principal) {
		String username = principal.getName();
		Optional<Member> memberOptional = memberService.getMemberByUsername(username);
		if (!memberOptional.isPresent()) {
			// Mitglied nicht gefunden
			return "error"; // Anpassen an Ihre Fehlerseite
		}
		Member member = memberOptional.get();
		model.addAttribute("member", member);

		// Holen Sie sich den Vertrag für das Mitglied
		MembershipContract contract = member.getContract(); // Je nach Ihrem Modell und Ihrer Geschäftslogik könnte dies
															// anders sein.
		if (contract != null) {
			model.addAttribute("contractId", contract.getId());
		} else {
			model.addAttribute("contractId", null);
			System.out.println("Kein Vertrag für dieses Mitglied gefunden");
		}
		return "member_personal_infos";
	}

	// Anzeige der View Contract Seite
	// @PreAuthorize("hasRole('USER')")
	@GetMapping("/ansicht_view_contract/{id}")
	public String showViewContract(@PathVariable("id") Long id, Model model) {
		Optional<MembershipContract> contractOptional = membershipContractRepository.findById(id);
		if (contractOptional.isPresent()) {
			MembershipContract contract = contractOptional.get();
			model.addAttribute("contract", contract);
			model.addAttribute("contractId", contract.getId());
			return "ansicht_view_contract";
		} else {
			 return "redirect:/ansicht_view_contract";
		}
	}
	@GetMapping("/ansicht_view_contract")
	public String showViewContract1(Model model) {
	    return "ansicht_view_contract";
	}

	
	
	
	
	
	
	
	
	
	@PostMapping("/submitContract")
	public String submitContract(@ModelAttribute("contract") MembershipContract contract) {
	    contract.setStatus("Pending");
	    contractRepository.save(contract);
	    return "redirect:/contracts";
	}
	
	// @PreAuthorize("hasRole('USER')")
	@GetMapping("/cancellation-form")
	public String showCancellationForm(@RequestParam("id") Long id, Model model, 
	                                   @RequestParam(value = "submitted", required = false) boolean submitted) {
	    Optional<MembershipContract> contractOptional = membershipContractRepository.findById(id);
	    if (contractOptional.isPresent()) {
	        MembershipContract contract = contractOptional.get();
	        model.addAttribute("contract", contract);
	        if (submitted) {
	            model.addAttribute("message", "Kündigungsantrag erfolgreich eingereicht.");
	        }
	        return "cancellation-form";
	    } else {
	        return "redirect:/contracts";
	    }
	}



	@PostMapping("/submitCancellation")
	public String submitCancellation(@RequestParam("id") Long id,
	                                 @RequestParam("reason") String reason,
	                                 Model model) {
	    Optional<MembershipContract> contractOptional = membershipContractRepository.findById(id);
	    if (contractOptional.isPresent()) {
	        MembershipContract contract = contractOptional.get();

	        CancellationRequest cancelRequest = new CancellationRequest();
	        cancelRequest.setContract(contract);
	        cancelRequest.setReason(reason);
	        cancelRequest.setApproval("Pending");
	        cancelRequest.setNoticePeriod(0);
	        cancelRequest.setMember(contract.getMember());
	        cancellationRequestRepository.save(cancelRequest);

	        Notification notification = new Notification();
	        notification.setMember(contract.getMember());
	        notification.setMessage("Ihr Kündigungsantrag wurde erfolgreich eingereicht und wird derzeit geprüft.");
	        notification.setDate(new Date());
	        notificationRepository.save(notification);

	        model.addAttribute("contract", contract);
	        model.addAttribute("message", "Kündigungsantrag wurde erfolgreich eingereicht!");
	    } else {
	        model.addAttribute("error", "Vertrag nicht gefunden!");
	    }
	    return "cancellation-form";
	}



	@GetMapping("/cancellation-requests")
	public String showCancellationRequests(Model model) {
	    List<CancellationRequest> cancellationRequests = cancellationRequestRepository.findByProcessed(false);
	    model.addAttribute("cancellationRequests", cancellationRequests);
	    return "cancellation-requests";
	}





	
	@GetMapping("/approveCancellation/{requestId}")
	public String approveCancellation(@PathVariable("requestId") Long requestId, Model model) {
	    Optional<CancellationRequest> cancellationRequestOptional = cancellationRequestRepository.findById(requestId);
	    if (cancellationRequestOptional.isPresent()) {
	        CancellationRequest cancellationRequest = cancellationRequestOptional.get();
	        
	        // Setzen der Genehmigung und Kündigungsfrist im CancellationRequest-Objekt
	        cancellationRequest.setApproval("Genehmigt");
	        cancellationRequest.setNoticePeriod(30);
	        cancellationRequest.setProcessed(true);
	        
	        cancellationRequestRepository.save(cancellationRequest);
	        //cancellationRequestRepository.delete(cancellationRequest);
	        model.addAttribute("cancellationRequest", cancellationRequest);

	        System.out.println("Genehmigungsstatus: " + cancellationRequest.getApproval());
	        return "redirect:/cancellation-requests";
	    } else {
	        model.addAttribute("error", "Kündigungsantrag nicht gefunden!");
	        return "error-page";
	    }
	}

	@GetMapping("/cancellation-approval")
	public String cancellationApproval(Model model) {
	    CancellationRequest cancellationRequest = new CancellationRequest();
	    cancellationRequest.setApproval("Genehmigt");
	    cancellationRequest.setNoticePeriod(30);
	    model.addAttribute("cancellationRequest", cancellationRequest);
	    return "cancellation-approval";
	}


}
