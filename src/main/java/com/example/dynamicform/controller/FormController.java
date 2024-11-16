package com.example.dynamicform.controller;

import com.example.dynamicform.model.FormData;
import com.example.dynamicform.service.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.util.List;

@Controller
public class FormController {

    @Autowired
    private FormService formDataService;

    /**
     * Endpoint for bulk uploading data via Excel file.
     *
     * @paramfile Excel file to upload.
     * @return ResponseEntity with status message.
     */
    @PostMapping("/bulk-upload")
    public ResponseEntity<String> bulkUpload(@RequestParam("files") MultipartFile[] files) {
        try {
            // Pass the files to the service layer
            formDataService.handleBulkUpload(files);

            // Redirect back to the main page after successful upload
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/")).build();
        } catch (Exception e) {
            // If something goes wrong, return an error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload files: " + e.getMessage());
        }
    }

    /**
     * Endpoint for uploading a single form entry.
     *
     * @param formData Data to save.
     * @return ResponseEntity with saved data.
     */
    @PostMapping("/upload-data")
    public String uploadData(@ModelAttribute FormData formData) {
        try {
            formDataService.saveFormData(formData);
            // Redirect back to the main page after successful data submission
            return "redirect:/";
        } catch (Exception e) {
            // Handle error and return an error page or message if needed
            return "error";
        }
    }

    /**
     * Endpoint for retrieving data based on name and email.
     *
     * @param name Name of the user.
     * @param email Email of the user.
     * @return List of matching FormData.
     */
    @GetMapping("/retrieve")
    public ResponseEntity<List<FormData>> retrieveData(
            @RequestParam String name,
            @RequestParam String email) {
        try {
            List<FormData> data = formDataService.retrieveFormData(name, email);
            if (data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
