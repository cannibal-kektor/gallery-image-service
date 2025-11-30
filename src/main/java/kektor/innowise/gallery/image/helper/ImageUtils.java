package kektor.innowise.gallery.image.helper;


public class ImageUtils {

    public static String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        String extension = filename.substring(filename.lastIndexOf("."));
        return extension.toLowerCase();
    }

}
