package com.lee.rokhan.common.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.EnumerationUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 扫描指定包下的所有类
 * @author lichujun
 */
@Slf4j
public class ScanUtils {

    private static final String URL_PROTOCOL_JAR = "jar";

    private static final String URL_PROTOCOL_ZIP = "zip";

    private static final String URL_PROTOCOL_WSJAR = "wsjar";

    private static final String URL_PROTOCOL_VFSZIP = "vfszip";

    private static final String URL_PROTOCOL_CODE_SOURCE = "code-source";

    private static final String JAR_URL_SEPARATOR = "!/";

    private static final String FILE_URL_PREFIX = "file:";

    private static final String FOLDER_SEPARATOR = "/";

    /**
     * 递归地在类路径中以指定的类加载器获取 dirPath 指定的目录下面所有的资源
     * 返回值一定不为 null。
     */
    private static ClassPathResource[] getClassPathResources(String dirPath, ClassLoader cl) throws IOException {
        URL[] roots = getRoots(dirPath, cl);
        Set<ClassPathResource> result = new LinkedHashSet<>(16);
        for (URL root : roots) {
            if (isJarResource(root)) {
                result.addAll(doFindPathMatchingJarResources(root));
            } else {
                result.addAll(doFindPathMatchingFileResources(root, dirPath));
            }
        }
        return result.toArray(new ClassPathResource[0]);
    }

    /**
     * 获取指定 basePackages
     * @param basePackages 这些包下面的类会被扫描
     * @return 能够扫描到的类
     */
    public static Set<Class<?>> getAllClassPathClasses(Set<String> basePackages)
            throws IOException {
        Set<Class<?>> classes = new HashSet<>();
        for (String basePackage : basePackages) {
            classes.addAll(getClasses(basePackage));
        }
        return classes;
    }

    /**
     * 使用 cl 指定的类加载器递归加载 packageName 指定的包名下面的所有的类。不会返回 null。
     */
    public static Set<Class<?>> getClasses(String packageName) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ClassPathResource[] resources = getClassPathResources(StringUtils.replace(packageName, ".", "/"), cl);
        Set<Class<?>> result = new HashSet<>();
        for (ClassPathResource resource : resources) {
            String urlPath = resource.getUrl().getPath();
            if (!urlPath.endsWith(".class") || urlPath.contains("$")) {
                continue;
            }
            Class<?> cls = resolveClass(cl, resource);
            if (cls != null) {
                result.add(cls);
            }
        }
        log.info("包名为{}已经扫描完毕...", packageName);
        return result;
    }

    private static Class<?> resolveClass(ClassLoader cl, ClassPathResource resource) {
        String className = resolveClassName(resource);
        try {
            return cl.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static String resolveClassName(ClassPathResource resource) {
        String path = resource.getClassPathPath();
        String className = path.substring(0, path.length() - ".class".length());
        className = StringUtils.replace(className, "/", ".");
        return className;
    }

    private static URL[] getRoots(String dirPath, ClassLoader cl) throws IOException {
        Enumeration<URL> resources = cl.getResources(dirPath);
        List<URL> resourceUrls = EnumerationUtils.toList(resources);
        return resourceUrls.toArray(new URL[0]);
    }

    private static Collection<ClassPathResource> doFindPathMatchingFileResources(URL rootUrl, String dirPath)
            throws IOException {
        String filePath = rootUrl.getFile();
        File file = new File(filePath);
        File rootDir = file.getAbsoluteFile();
        return doFindMatchingFileSystemResources(rootDir, dirPath);
    }

    private static Collection<ClassPathResource> doFindMatchingFileSystemResources(File rootDir, String dirPath)
            throws IOException {
        Set<File> allFiles = new LinkedHashSet<>();
        retrieveAllFiles(rootDir, allFiles);
        String classPathRoot = parseClassPathRoot(rootDir, dirPath);
        Set<ClassPathResource> result = new LinkedHashSet<>(allFiles.size());
        for (File file : allFiles) {
            String absolutePath = file.getAbsolutePath();
            URL url = new URL("file:///" + absolutePath);
            String classPathPath = absolutePath.substring(classPathRoot.length());
            classPathPath = StringUtils.replace(classPathPath, "\\", "/");
            result.add(new ClassPathResource(url, classPathPath));
        }
        return result;
    }

    private static String parseClassPathRoot(File rootDir, String dirPath) {
        String absolutePath = rootDir.getAbsolutePath();
        absolutePath = StringUtils.replace(absolutePath, "\\", "/");
        int lastIndex = absolutePath.lastIndexOf(dirPath);
        String result = absolutePath.substring(0, lastIndex);
        if (!result.endsWith(FOLDER_SEPARATOR)) {
            result = result + FOLDER_SEPARATOR;
        }
        return result;
    }

    private static void retrieveAllFiles(File dir, Set<File> allFiles) {
        File[] subFiles = dir.listFiles();
        assert subFiles != null;
        allFiles.addAll(Arrays.asList(subFiles));

        for (File subFile : subFiles) {
            if (subFile.isDirectory()) {
                retrieveAllFiles(subFile, allFiles);
            }
        }
    }

    private static Collection<ClassPathResource> doFindPathMatchingJarResources(URL rootUrl) throws IOException {
        URLConnection con = rootUrl.openConnection();
        JarFile jarFile;
        String rootEntryPath;
        boolean newJarFile = false;

        if (con instanceof JarURLConnection) {
            JarURLConnection jarCon = (JarURLConnection) con;
            jarCon.setUseCaches(true);
            jarFile = jarCon.getJarFile();
            JarEntry jarEntry = jarCon.getJarEntry();
            rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
        } else {
            String urlFile = rootUrl.getFile();
            int separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR);
            if (separatorIndex != -1) {
                String jarFileUrl = urlFile.substring(0, separatorIndex);
                rootEntryPath = urlFile.substring(separatorIndex + JAR_URL_SEPARATOR.length());
                jarFile = getJarFile(jarFileUrl);
            } else {
                jarFile = new JarFile(urlFile);
                rootEntryPath = "";
            }
            newJarFile = true;
        }

        try {
            if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith(FOLDER_SEPARATOR)) {
                rootEntryPath = rootEntryPath + "/";
            }
            Set<ClassPathResource> result = new LinkedHashSet<>(8);
            for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                JarEntry entry = entries.nextElement();
                String entryPath = entry.getName();
                if (entryPath.startsWith(rootEntryPath)) {
                    String relativePath = entryPath.substring(rootEntryPath.length());
                    String rootPath = rootUrl.getPath();
                    rootPath = rootPath.endsWith(FOLDER_SEPARATOR) ? rootPath : rootPath +FOLDER_SEPARATOR;
                    String newPath = applyRelativePath(rootPath, relativePath);
                    String classPathPath = applyRelativePath(rootEntryPath, relativePath);
                    result.add(new ClassPathResource(new URL(newPath), classPathPath));
                }
            }
            return result;
        } finally {
            if (newJarFile) {
                jarFile.close();
            }
        }
    }

    private static String applyRelativePath(String path, String relativePath) {
        int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
        if (separatorIndex != -1) {
            String newPath = path.substring(0, separatorIndex);
            if (!relativePath.startsWith(FOLDER_SEPARATOR)) {
                newPath += FOLDER_SEPARATOR;
            }
            return newPath + relativePath;
        } else {
            return relativePath;
        }
    }

    private static JarFile getJarFile(String jarFileUrl) throws IOException {
        if (jarFileUrl.startsWith(FILE_URL_PREFIX)) {
            try {
                return new JarFile(toURI(jarFileUrl).getSchemeSpecificPart());
            } catch (URISyntaxException ex) {
                return new JarFile(jarFileUrl.substring(FILE_URL_PREFIX.length()));
            }
        } else {
            return new JarFile(jarFileUrl);
        }
    }

    private static URI toURI(String location) throws URISyntaxException {
        return new URI(StringUtils.replace(location, " ", "%20"));
    }

    private static boolean isJarResource(URL url) {
        String protocol = url.getProtocol();
        return (URL_PROTOCOL_JAR.equals(protocol) || URL_PROTOCOL_ZIP.equals(protocol) ||
                URL_PROTOCOL_VFSZIP.equals(protocol) || URL_PROTOCOL_WSJAR.equals(protocol) ||
                (URL_PROTOCOL_CODE_SOURCE.equals(protocol) && url.getPath().contains(JAR_URL_SEPARATOR)));
    }

    /**
     * 类路径资源。
     */
    @Getter
    public static class ClassPathResource {
        /**
         * 此资源对应的 URL 对象。
         */
        private URL url;

        /**
         * 类路径下的路径。特点是这个路径字符串去掉了类路径的“根”部分。
         */
        private String classPathPath;

        /**
         * ctor.
         */
        ClassPathResource(URL url, String classPathPath) {
            this.url = url;
            this.classPathPath = classPathPath;
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }
}