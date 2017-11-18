package pl.sdadas.fsbrowser;

public final class Version {

    private static final String VERSION = "${project.version}";

    private static final String GROUP_ID = "${project.groupId}";

    private static final String ARTIFACT_ID = "${project.artifactId}";

    private static final String REVISION = "${buildNumber}";

    public static String getVersion() {
        return VERSION;
    }

    public static String getGroupId() {
        return GROUP_ID;
    }

    public static String getArtifactId() {
        return ARTIFACT_ID;
    }

    public static String getRevision() {
        return REVISION;
    }

    public static String getFullVersionString() {
        return String.format("%s-%s-%s", ARTIFACT_ID, VERSION, REVISION);
    }
}