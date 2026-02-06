package vn.hutech.trandinhty_2280618597.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import vn.hutech.trandinhty_2280618597.entities.Book;
import vn.hutech.trandinhty_2280618597.entities.Category;

@Service
public class BookImportService {

    @Autowired
    private BookService bookService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FileStorageService fileStorageService;

    public void importBooks(MultipartFile excelFile) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(excelFile.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                // Configure cell mapping:
                // 0: Title, 1: Author, 2: Price, 3: CategoryName, 4: ImagePath (Local), 5:
                // Description, 6: Quantity

                Book book = new Book();

                // Title
                if (row.getCell(0) != null)
                    book.setTitle(row.getCell(0).getStringCellValue());

                // Author
                if (row.getCell(1) != null)
                    book.setAuthor(row.getCell(1).getStringCellValue());

                // Price
                if (row.getCell(2) != null)
                    book.setPrice(row.getCell(2).getNumericCellValue());

                // Category
                if (row.getCell(3) != null) {
                    String categoryName = row.getCell(3).getStringCellValue();
                    // Assume category exists or find by name. For simplicity, just finding all and
                    // filtering.
                    // Ideally CategoryService should have findByName
                    Category category = categoryService.getAllCategories().stream()
                            .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                            .findFirst()
                            .orElse(null);
                    book.setCategory(category);
                }

                // Image Handling
                if (row.getCell(4) != null) {
                    String localPath = row.getCell(4).getStringCellValue();
                    File localFile = new File(localPath);
                    List<String> imageUrls = new ArrayList<>();

                    if (localFile.exists() && localFile.isFile()) {
                        try {
                            String uploadedFileName = fileStorageService.storeFile(localFile);
                            imageUrls.add("/uploads/" + uploadedFileName);
                        } catch (Exception e) {
                            e.printStackTrace();
                            // If fail, maybe add placeholder or skip
                        }
                    }
                    book.setImageUrls(imageUrls);
                }

                // Description
                if (row.getCell(5) != null)
                    book.setDescription(row.getCell(5).getStringCellValue());

                // Quantity (Số lượng tồn kho)
                if (row.getCell(6) != null) {
                    if (row.getCell(6).getCellType() == CellType.NUMERIC) {
                        book.setQuantity((int) row.getCell(6).getNumericCellValue());
                    } else if (row.getCell(6).getCellType() == CellType.STRING) {
                        try {
                            book.setQuantity(Integer.parseInt(row.getCell(6).getStringCellValue()));
                        } catch (NumberFormatException e) {
                            book.setQuantity(0);
                        }
                    }
                } else {
                    book.setQuantity(0);
                }

                bookService.saveBook(book);
            }
        }
    }
}
