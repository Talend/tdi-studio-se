package org.talend.designer.publish.core.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.talend.designer.publish.core.models.BundleModel;
import org.talend.designer.publish.core.models.FeaturesModel;

public class ZipModel {

    private static final String PREFIX = "repository/";

    private final ZipOutputStream output;

    private byte[] buf;

    public ZipModel(FeaturesModel featuresModel, File destination) throws IOException {

        // Create the parent file if not exist
        File parentDestFile = destination.getParentFile();
        if (!parentDestFile.exists()) {
            parentDestFile.mkdirs();
        }
        output = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destination)));

        /*
         * feature file path: repository/[projectName]/[itemName]/[itemName]-feature
         * /[itemVersion]/[itemName]-[itemVersion]-feature.xml
         */
        String featurePrefix = new StringBuilder(PREFIX)
            .append(featuresModel.getGroupId().replace('.', '/')).append('/')
            .append(featuresModel.getArtifactId()).append('/')
            .append(featuresModel.getVersion()).append('/')
            .append(featuresModel.getArtifactId()).append('-').append(featuresModel.getVersion()).append(".xml").toString();
        add(featurePrefix, featuresModel.getContent().getBytes());

        /*
         * Bundle File path: repository/[projectName]/[itemName]/[itemName]-bundle
         * /[itemVersion]/[itemName]-bundle-[itemVersion].jar
         */
        for (BundleModel bundleModel : featuresModel.getBundles()) {
            // add bundle jar file
            File f = bundleModel.getFile();
            if (null == f) {
                continue;
            }
            add(new StringBuilder(PREFIX)
                .append(bundleModel.getGroupId().replace('.', '/')).append('/')
                .append(bundleModel.getArtifactId()).append('/')
                .append(bundleModel.getVersion()).append('/')
                .append(bundleModel.getArtifactId()).append('-').append(bundleModel.getVersion()).append(".jar").toString(),
                f);
        }

    }

    public void add(String location, File f) throws IOException {
        if (null == buf) {
            buf = new byte[1024];
        }
        ZipEntry entry = new ZipEntry(location);
        entry.setSize(f.length());
        entry.setTime(f.lastModified());
        output.putNextEntry(entry);

        // write file content
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(f));
            int readLen = 0;
            while ((readLen = is.read(buf)) != -1) {
                output.write(buf, 0, readLen);
            }
        } finally {
            is.close();
        }
    }

    public void add(String location, byte[] content) throws IOException {
        ZipEntry entry = new ZipEntry(location);
        entry.setSize(content.length);
        entry.setTime(System.currentTimeMillis());
        output.putNextEntry(entry);
        output.write(content);
    }

    public void close() {
        try {
            output.flush();
        } catch (IOException e) {
        } finally {
            try {
                output.close();
            } catch (IOException e) {
            }
        }
    }
}
