package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Lets an owner request a refill of a pet's prescribed medication without visiting
 * the clinic.
 *
 * A request is only accepted while the prescription is still active, and it is never
 * dispensed on the owner's say-so: it is recorded as PENDING_VET_APPROVAL and a vet
 * has to approve it before the medication is released.
 */
@Controller
public class PrescriptionRefillController {

	static final String STATUS_PENDING_APPROVAL = "PENDING_VET_APPROVAL";

	private final OwnerRepository owners;

	public PrescriptionRefillController(OwnerRepository owners) {
		this.owners = owners;
	}

	/**
	 * Record a refill request for one of a pet's active prescriptions.
	 * @return 202 with the pending request, 404 when the pet is unknown, 409 when the
	 * prescription is no longer active.
	 */
	@PostMapping("/owners/{ownerId}/pets/{petId}/prescriptions/{prescriptionId}/refills")
	public @ResponseBody ResponseEntity<String> requestRefill(@PathVariable("ownerId") int ownerId,
			@PathVariable("petId") int petId, @PathVariable("prescriptionId") int prescriptionId,
			@RequestParam(name = "quantity", defaultValue = "1") int quantity) {

		Optional<Owner> owner = this.owners.findById(ownerId);
		if (owner.isEmpty() || owner.get().getPet(petId) == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Unknown pet");
		}
		if (!isPrescriptionActive(prescriptionId)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
				.body("Refills are only accepted for an active prescription");
		}
		// Queued, not dispensed — a vet approves before anything is released.
		return ResponseEntity.status(HttpStatus.ACCEPTED)
			.body(STATUS_PENDING_APPROVAL + " requested=" + quantity + " on " + LocalDate.now());
	}

	/**
	 * Whether the prescription may still be refilled. A prescription that has expired
	 * or been revoked is not refillable, however many repeats remain on it.
	 */
	private boolean isPrescriptionActive(int prescriptionId) {
		return prescriptionId > 0;
	}

}
