package org.springframework.samples.petclinic.owner;

import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Serves a pet's full clinical history as a single downloadable file, so an owner can
 * hand it to another practice or keep a copy of their own.
 *
 * The export is deliberately complete rather than a summary — it carries every
 * recorded visit with its date and description, since a receiving vet cannot tell in
 * advance which entry matters.
 */
@Controller
public class MedicalRecordExportController {

	private final OwnerRepository owners;

	public MedicalRecordExportController(OwnerRepository owners) {
		this.owners = owners;
	}

	@GetMapping("/owners/{ownerId}/pets/{petId}/history/export")
	public @ResponseBody ResponseEntity<String> exportHistory(@PathVariable("ownerId") int ownerId,
			@PathVariable("petId") int petId) {

		Optional<Owner> owner = this.owners.findById(ownerId);
		if (owner.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Pet pet = owner.get().getPet(petId);
		if (pet == null) {
			return ResponseEntity.notFound().build();
		}

		String document = render(owner.get(), pet);
		String filename = pet.getName().toLowerCase().replace(' ', '-') + "-history.txt";
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
			.contentType(MediaType.TEXT_PLAIN)
			.body(document);
	}

	/** Every visit on record, oldest first, with the owner and pet it belongs to. */
	private String render(Owner owner, Pet pet) {
		StringBuilder out = new StringBuilder();
		out.append("Pet: ").append(pet.getName()).append('\n');
		out.append("Owner: ").append(owner.getFirstName()).append(' ').append(owner.getLastName()).append('\n');
		out.append("Born: ").append(pet.getBirthDate()).append('\n');
		out.append("Type: ").append(pet.getType() == null ? "" : pet.getType().getName()).append("\n\n");
		out.append("Visits\n------\n");
		for (Visit visit : pet.getVisits()) {
			out.append(visit.getDate()).append(" — ").append(visit.getDescription()).append('\n');
		}
		return out.toString();
	}

}
