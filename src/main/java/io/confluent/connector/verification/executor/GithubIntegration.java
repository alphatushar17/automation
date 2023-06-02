package io.confluent.connector.verification.executor;

import io.confluent.connector.verification.constants.ApplicationConstants;
import io.confluent.connector.verification.model.ReleaseInfo;
import io.confluent.connector.verification.repository.ReleaseInfoRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class GithubIntegration {
	
	private static final Logger logger = LoggerFactory.getLogger(GithubIntegration.class);
	private static final String AUTHORIZATION_HEADER = ApplicationConstants.GITHUB_AUTHORIZATION_HEADER;
    private static String buildPath;

    @Value("${source_download_location}")
    private String download_to_location;

    @Value("${docker_volume_location}")
    private String docker_volume_location;

    @Value("${source_unzip_location}")
    private String unzip_location;

    @Autowired
    private ReleaseInfoRepository releaseInfoRepository;

    public Set<ReleaseInfo> getPartnerRepoDetails(List<String> partnerIds) throws IOException {

        Set<ReleaseInfo> releaseInfos = new HashSet<>();
        String releaseInfo, releaseUrl;
        List<ReleaseInfo> partnerReleaseInfo;
        if(partnerIds.size() == 1) {
            partnerReleaseInfo = releaseInfoRepository.findAllOverride();
        } else {
            partnerReleaseInfo = releaseInfoRepository.getReleaseInfoList(partnerIds);
        }
        if(partnerReleaseInfo.isEmpty())
            return null;
        for (ReleaseInfo record : partnerReleaseInfo) {

            if (record.getLastReleaseVersion() != null) {
                Request request = new Request.Builder()
                       .url(record.getGitReleaseURL())
                       .method("GET", null)
                       .addHeader("Accept", "application/json; q=0.5")
                       .addHeader("Accept", "application/vnd.github.v3+json")
                       .addHeader("Authorization", AUTHORIZATION_HEADER)
                       .build();
                OkHttpClient client = new OkHttpClient().newBuilder().build();
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    releaseInfo = Objects.requireNonNull(response.body()).string();
                    releaseUrl = getReleaseFileURLLocation(releaseInfo, record.getRepoTagName());

                    if (!Objects.requireNonNull(getTagName(releaseInfo)).equalsIgnoreCase(record.getCurrentReleaseVersion())) {

                        // update currentReleaseVersion
                        record.setCurrentReleaseVersion(Objects.requireNonNull(getTagName(releaseInfo)));
                        releaseInfoRepository.save(record);

                        downloadFile(releaseUrl, download_to_location, record.getPartnerName());
                        unzip((download_to_location + record.getPartnerName() + ".zip"), unzip_location);
                        logger.info("Download & Extract process completed successfully.");

                        buildPath = String.valueOf(Paths.get(buildPath).resolve(record.getBuildPath()));
                        RunCommand(buildPath, record.getBuildCommand());
                        Path source = Paths.get(buildPath).resolve("target").resolve(record.getFinalJarName());
                        File directory = new File(docker_volume_location);
                        if (!directory.exists()) {
                            directory.mkdir();
                        }
                        Path dest = Paths.get(docker_volume_location).resolve(source.getFileName());
                        FileCopyUtils.copy(new File(String.valueOf(source)), new File(String.valueOf(dest)));
                    } else {
                        logger.info("Partner details are up to date. Hence skipping verification process");
                    }
                } else {
                   logger.info("Error in getting release info");
                }
            } else {
                //clone repo directly
                RunCommand(unzip_location, "git clone " +record.getGitReleaseURL());
                buildPath = String.valueOf(Paths.get(unzip_location).resolve(record.getBuildPath()));
                RunCommand(buildPath, record.getBuildCommand());

                Path source = Paths.get(buildPath).resolve("target").resolve(record.getFinalJarName());
                File directory = new File(docker_volume_location);
                if (!directory.exists()) {
                    directory.mkdir();
                }
                Path dest = Paths.get(docker_volume_location).resolve(source.getFileName());
                if(new File(String.valueOf(source)).isDirectory())
                    copyDirectoryFileVisitor(String.valueOf(source), String.valueOf(dest));
                else
                    FileCopyUtils.copy(new File(String.valueOf(source)), new File(String.valueOf(dest)));
           }
           releaseInfos.add(record);
        }
        return releaseInfos;
	}

    private static void copyDirectoryFileVisitor(String source, String target)
            throws IOException {

        TreeCopyFileVisitor fileVisitor = new TreeCopyFileVisitor(source, target);
        Files.walkFileTree(Paths.get(source), fileVisitor);

    }

	private static String getTagName(String strLatestReleaseInfo) {
		JSONObject jsonLatestReleaseInfo = new JSONObject(strLatestReleaseInfo);
		if (jsonLatestReleaseInfo.has("tag_name")) {
            logger.info(jsonLatestReleaseInfo.get("tag_name").toString());
            return jsonLatestReleaseInfo.get("tag_name").toString();
        }
		return null;
	}
	
	private static String getReleaseFileURLLocation(String strLatestReleaseInfo, String tagName) {
		JSONObject jsonLatestReleaseInfo = new JSONObject(strLatestReleaseInfo);
		logger.info(jsonLatestReleaseInfo.get(tagName).toString());
		return jsonLatestReleaseInfo.get(tagName).toString();
	}

	private static void downloadFile(String fileURLLocation, String toLocation, String fileName) {
		ProgressCallback progressCallback = progressInBytes -> logger.info("bytes downloaded ::" + progressInBytes);

        logger.info("Ready to download");
        File directory = new File(toLocation);
        if (! directory.exists()) {
            directory.mkdir();
        }

        toLocation = Paths.get(toLocation).resolve(fileName+".zip").toString();
		try (BinaryFileWriter writer = new BinaryFileWriter(new FileOutputStream(toLocation), progressCallback);
            BinaryFileDownloader downloader = new BinaryFileDownloader(new OkHttpClient(), writer)) {
            downloader.download(fileURLLocation);
            new File(toLocation);
        } catch (Exception e) {
        	logger.error("An unexpected exception has occurred: " + e);
        }
	}

	private static void unzip(String strZipFilePath, String strDestDir) throws IOException {

        buildPath = null;
        final File destDir = new File(strDestDir);
        final byte[] buffer = new byte[1024];
        final ZipInputStream zis = new ZipInputStream(new FileInputStream(strZipFilePath));
        ZipEntry zipEntry = zis.getNextEntry();

        while (zipEntry != null) {
            final File newFile = newFile(destDir, zipEntry);
            if(buildPath == null)
                buildPath = newFile.getAbsolutePath();
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                final FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
	}
	
	private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

	private static void RunCommand(String path, String command) {

        try {
            boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(new File(path));
            builder.redirectErrorStream(true);
            if (isWindows) {
                builder.command("cmd.exe", "/c", command);
            } else {
                builder.command("sh", "-c", command);
            }
            logger.info("Ready to build package");
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
            	logger.info(line);
            }
            int exitCode = process.waitFor();
            logger.info("\n Exited with error code : " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
        }       
    }

}