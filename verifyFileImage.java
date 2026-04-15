import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class verifyFileImage {

    // Maximum file size: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // Allowed image extensions
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    ));

    /**
     * Comprehensive file verification for image uploads
     * 
     * @param filePath Path to the file to verify
     * @return VerificationResult containing status and detailed messages
     */
    public static VerificationResult verifyImageFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return new VerificationResult(false, "File path is empty.");
        }

        File file = new File(filePath);

        // Check if file exists
        if (!file.exists()) {
            return new VerificationResult(false, "File does not exist: " + filePath);
        }

        // Check if it's actually a file (not a directory)
        if (!file.isFile()) {
            return new VerificationResult(false, "Path is not a file: " + filePath);
        }

        // Check file size
        long fileSize = file.length();
        if (fileSize == 0) {
            return new VerificationResult(false, "File is empty.");
        }
        if (fileSize > MAX_FILE_SIZE) {
            return new VerificationResult(false, String.format(
                    "File size exceeds limit. Maximum: %.1f MB, Actual: %.1f MB",
                    MAX_FILE_SIZE / (1024.0 * 1024.0),
                    fileSize / (1024.0 * 1024.0)
            ));
        }

        // Check file extension
        String fileName = file.getName();
        String extension = getFileExtension(fileName);

        if (extension.isEmpty()) {
            return new VerificationResult(false, "File has no extension.");
        }

        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            return new VerificationResult(false, String.format(
                    "Unsupported file format: .%s. Allowed formats: %s",
                    extension,
                    String.join(", ", ALLOWED_EXTENSIONS)
            ));
        }

        // Verify it's actually a valid image file
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                return new VerificationResult(false, "File is not a valid image. The file may be corrupted or in an unsupported format.");
            }

            // Check image dimensions
            int width = image.getWidth();
            int height = image.getHeight();
            if (width <= 0 || height <= 0) {
                return new VerificationResult(false, "Image has invalid dimensions.");
            }

            // All checks passed
            return new VerificationResult(true, String.format(
                    "Image verified successfully. Size: %.1f KB | Dimensions: %dx%d | Format: .%s",
                    fileSize / 1024.0,
                    width,
                    height,
                    extension.toUpperCase()
            ));

        } catch (java.io.IOException e) {
            return new VerificationResult(false, "Could not read image file: " + e.getMessage());
        } catch (Exception e) {
            return new VerificationResult(false, "Unexpected error during verification: " + e.getMessage());
        }
    }

    /**
     * Extract file extension from filename
     */
    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot + 1);
        }
        return "";
    }

    /**
     * Inner class to hold verification result
     */
    public static class VerificationResult {
        public final boolean isValid;
        public final String message;

        public VerificationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }

        @Override
        public String toString() {
            return (isValid ? "✓ VALID: " : "✗ INVALID: ") + message;
        }
    }
}
