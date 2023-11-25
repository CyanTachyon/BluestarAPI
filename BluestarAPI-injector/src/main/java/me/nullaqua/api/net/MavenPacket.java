package me.nullaqua.api.net;

import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

/**
 * Represents a dependency that needs to be downloaded and injected into the
 * classpath
 *
 * @author Zach Deibert, sky
 * @since 1.0.0
 */
public class MavenPacket extends AbstractXmlParser
{

    /**
     * 当版本尚未指定时的占位符字符串
     */
    private static final String LATEST_VERSION="latest";

    /**
     * 此依赖项的组 ID
     */
    private final String groupId;

    /**
     * 此依赖项的工件 ID
     */
    private final String artifactId;

    /**
     * 依赖项的范围
     */
    private final MavenPacketScope scope;

    /**
     * 要下载的版本，或者如果在 pom 中没有指定依赖项的最新版本，则设置依赖项的最新版本
     */
    private String version;

    public MavenPacket(Element node) throws ParseException
    {
        this(find("groupId",node),
             find("artifactId",node),
             find("version",node,LATEST_VERSION),
             MavenPacketScope.valueOf(find("scope",node,"runtime").toUpperCase()));
    }

    public MavenPacket(String groupId,String artifactId,String version,MavenPacketScope scope)
    {
        this.groupId=groupId;
        this.artifactId=artifactId;
        this.version=version.contains("$")||version.contains("[")||version.contains("(")?LATEST_VERSION:version;
        this.scope=scope;
    }

    /**
     * 获取依赖的下载地址
     */
    public URL getURL(Repository repo,String ext) throws MalformedURLException
    {
        String name=String.format("%s-%s.%s",getArtifactId(),getVersion(),ext);
        return new URL(String.format("%s/%s/%s/%s/%s",
                                     repo.getUrl(),
                                     getGroupId().replace('.','/'),
                                     getArtifactId(),
                                     getVersion(),
                                     name));
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version.equals(LATEST_VERSION)?null:version;
    }

    /**
     * Sets the version of this dependency
     */
    public void setVersion(String version)
    {
        if (!this.version.equals(LATEST_VERSION))
        {
            throw new IllegalStateException("Version is already resolved");
        }
        else if (version.equals(LATEST_VERSION))
        {
            throw new IllegalArgumentException("Cannot set version to the latest");
        }
        else
        {
            this.version=version;
        }
    }

    public String getGroupId()
    {
        return groupId;
    }

    /**
     * Get the latest version of this artifact that are currently
     * downloaded on this computer
     */
    @Nullable
    public DependencyVersion getInstalledLatestVersion(File baseDir)
    {
        DependencyVersion max=null;
        for (DependencyVersion ver: getInstalledVersions(baseDir))
        {
            if (max==null||ver.compareTo(max)>0)
            {
                max=ver;
            }
        }
        return max;
    }

    /**
     * Gets a list of all the versions of this artifact that are currently
     * downloaded on this computer
     *
     * @return An array of the versions that are already downloaded
     */
    public DependencyVersion[] getInstalledVersions(File dir)
    {
        for (String part: getGroupId().split("\\."))
        {
            dir=new File(dir,part);
        }
        dir=new File(dir,getArtifactId());
        String[] list=dir.list();
        if (list==null)
        {
            return new DependencyVersion[0];
        }
        DependencyVersion[] versions=new DependencyVersion[list.length];
        for (int i=0;i<list.length;++i)
        {
            versions[i]=new DependencyVersion(list[i]);
        }
        return versions;
    }

    /**
     * Gets the file that the downloaded artifact should be stored in
     *
     * @param dir The directory to store downloaded artifacts in
     * @param ext The file extension to download (should be either <code>"jar"</code> or <code>"pom"</code>)
     * @return The file to download into
     */
    public File findFile(File dir,String ext)
    {
        if (getVersion()==null)
        {
            throw new IllegalStateException("Version is not resolved: "+this);
        }
        for (String part: getGroupId().split("\\."))
        {
            dir=new File(dir,part);
        }
        dir=new File(dir,getArtifactId());
        dir=new File(dir,getVersion());
        dir=new File(dir,String.format("%s-%s.%s",getArtifactId(),getVersion(),ext));
        return dir;
    }

    public MavenPacketScope getScope()
    {
        return scope;
    }

    @Override
    public String toString()
    {
        return String.format("%s:%s:%s",groupId,artifactId,version);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this==o)
        {
            return true;
        }
        if (!(o instanceof MavenPacket))
        {
            return false;
        }
        MavenPacket that=(MavenPacket) o;
        return Objects.equals(getGroupId(),that.getGroupId())&&
               Objects.equals(getArtifactId(),that.getArtifactId())&&
               Objects.equals(getVersion(),that.getVersion());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getGroupId(),getArtifactId());
    }

    public enum MavenPacketScope
    {
        COMPILE,
        PROVIDED,
        RUNTIME,
        TEST,
        SYSTEM,
        IMPORT
    }

    public static class DependencyVersion implements Comparable<DependencyVersion>
    {

        /**
         * 版本的组件列表
         */
        private final List<Integer> parts;

        /**
         * 作为字符串的版本
         */
        private final String version;

        public DependencyVersion(String version)
        {
            parts=new ArrayList<>();
            for (String part: version.split("[^0-9]"))
            {
                if (!part.isEmpty())
                {
                    parts.add(Integer.parseInt(part));
                }
            }
            this.version=version;
        }

        @Override
        public int compareTo(DependencyVersion o)
        {
            Iterator<Integer> us=parts.iterator();
            Iterator<Integer> them=o.parts.iterator();
            while (us.hasNext()&&them.hasNext())
            {
                int diff=us.next().compareTo(them.next());
                if (diff!=0)
                {
                    return diff;
                }
            }
            return us.hasNext()?1:them.hasNext()?-1:0;
        }

        @Override
        public String toString()
        {
            return version;
        }
    }
}