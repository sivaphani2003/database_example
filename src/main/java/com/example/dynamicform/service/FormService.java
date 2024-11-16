package com.example.dynamicform.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.example.dynamicform.model.FormData;
import com.example.dynamicform.repository.FormDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class FormService {
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";  // Return an empty string if the cell is null
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // If the cell contains a numeric value, check if it's a date or a regular number
                if (DateUtil.isCellDateFormatted(cell)) {
                    // If it's a date, format it as a string (you can customize the format)
                    return cell.getDateCellValue().toString();
                } else {
                    // Otherwise, return the numeric value as a string
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";  // Return an empty string for other types (e.g., blank cells)
        }
    }

    // Declare the FormDataRepository instance variable
    private final FormDataRepository formDataRepository;

    // Constructor injection to ensure Spring injects the FormDataRepository
    @Autowired
    public FormService(FormDataRepository formDataRepository) {
        this.formDataRepository = formDataRepository;  // Initialize the repository
    }

    /**
     * Handle bulk file upload.
     * This method processes each file, parses its content, and saves the data to MongoDB.
     * @param files the uploaded Excel files
     * @throws Exception if any error occurs during file processing or saving
     */
    public void handleBulkUpload(MultipartFile[] files) throws Exception {
        // Iterate through the uploaded files
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new Exception("File is empty");
            }
            // Parse the file and get the list of FormData objects
            List<FormData> dataList = parseExcelFile(file);
            // Save each parsed FormData object to the database
            for (FormData data : dataList) {
                formDataRepository.save(data);  // Save to MongoDB
            }
        }
    }

    /**
     * Parse the Excel file and convert its rows into a list of FormData objects.
     * @param file the uploaded Excel file
     * @return a list of FormData objects representing the rows in the Excel file
     * @throws Exception if there is an error while reading or parsing the Excel file
     */
    private List<FormData> parseExcelFile(MultipartFile file) throws Exception {
        // List to hold parsed FormData objects
        List<FormData> dataList = new ArrayList<>();

        // Read the Excel file
        try (InputStream inputStream = file.getInputStream()) {
            // Create a workbook instance
            Workbook workbook = new XSSFWorkbook(inputStream);
            // Get the first sheet in the workbook (assuming the data is in the first sheet)
            Sheet sheet = workbook.getSheetAt(0);

            // Iterate through each row in the sheet
            for (Row row : sheet) {
                // Skip the header row (optional)
                if (row.getRowNum() == 0) {
                    continue;
                }

                // Create a new FormData object for each row
                FormData dataModel = new FormData();
                // Parse the cells and set the values in the FormData object
                dataModel.setFirstName(getCellValueAsString(row.getCell(0))); // Column 1
                dataModel.setLastName(getCellValueAsString(row.getCell(1)));  // Column 2
                dataModel.setPhoneNumber(getCellValueAsString(row.getCell(2)));  // Column 3
                dataModel.setEmail(getCellValueAsString(row.getCell(3)));  // Column 4
                dataModel.setAdditionalFields(getCellValueAsString(row.getCell(4)));  // Column 5

                // Add the FormData object to the dataList
                dataList.add(dataModel);
            }
        } catch (Exception e) {
            // Handle exceptions (e.g., file reading or parsing errors)
            throw new Exception("Error parsing Excel file: " + e.getMessage());
        }

        return dataList;
    }

    /**
     * Save a single FormData object to MongoDB.
     * @param formData the FormData object to save
     * @return the saved FormData object
     */
    public FormData saveFormData(FormData formData) {
        return formDataRepository.save(formData);  // Save using the repository
    }

    /**
     * Retrieve FormData records based on the first name and email.
     * @param name the first name to search by
     * @param email the email to search by
     * @return a list of FormData objects that match the search criteria
     */
    public List<FormData> retrieveFormData(String name, String email) {
        return formDataRepository.findByFirstNameAndEmail(name, email);  // Find records by first name and email
    }
}
