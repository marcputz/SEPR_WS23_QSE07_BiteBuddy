package at.ac.tuwien.sepr.groupphase.backend.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.util.Optional;


public class ResourceFileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String DEFAULT_CLASSPATH = "classpath:";

    private String folderPath;
    private Optional<String> loaderFolderPath;

    public ResourceFileUtils(String folderPath, Optional<String> loaderFolderPath) {
        this.folderPath = folderPath;
        this.loaderFolderPath = loaderFolderPath;

    }

    public File getResourceFile(String filename) throws FileNotFoundException {
        LOGGER.trace("getResourceFile({})", filename);
        return (filename == null) ? ResourceUtils.getFile(DEFAULT_CLASSPATH + folderPath)
            : ResourceUtils.getFile(DEFAULT_CLASSPATH + folderPath + "/" + filename);
    }

    public Resource getResourceLoader(ResourceLoader resourceLoader, String filename) throws FileNotFoundException {
        LOGGER.trace("getResourceLoader({})", filename);
        return loaderFolderPath.map(s -> resourceLoader.getResource(DEFAULT_CLASSPATH + s + "/" + filename)).orElse(null);
    }

}
