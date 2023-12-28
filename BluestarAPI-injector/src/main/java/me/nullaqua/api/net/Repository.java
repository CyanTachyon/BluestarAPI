package me.nullaqua.api.net;

import me.nullaqua.api.net.dowloader.Downloader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Objects;

public class Repository extends AbstractXmlParser
{
    private static final String MAVEN_CENTRAL="https://maven.aliyun.com/repository/central";

    private final String url;

    public Repository()
    {
        this(MAVEN_CENTRAL);
    }

    public Repository(String url)
    {
        this.url=url.endsWith("/")?url.substring(0,url.length()-1):url;
    }

    public Repository(Element node) throws ParseException
    {
        this(find("url",node,null));
    }

    public void downloadFile(MavenPacket dep,File out) throws IOException
    {
        String ext=out.getName().substring(out.getName().lastIndexOf('.')+1);
        URL url=dep.getURL(this,ext);

        Downloader.download(url.toString(),out.getAbsolutePath()).waitSuccess();
        Downloader.download(dep.getURL(this,ext+".sha1").toString(),new File(out.getPath()+".sha1").getAbsolutePath())
                  .waitSuccess();
    }

    public void getLatestVersion(MavenPacket dep) throws IOException
    {
        URL url=new URL(String.format("%s/%s/%s/maven-metadata.xml",
                                      getUrl(),
                                      dep.getGroupId().replace('.','/'),
                                      dep.getArtifactId()));
        try
        {
            DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
            DocumentBuilder builder=factory.newDocumentBuilder();
            InputStream ins=url.openStream();
            Document doc=builder.parse(ins);
            dep.setVersion(find("release",doc.getDocumentElement(),find("version",doc.getDocumentElement(),null)));
        }
        catch (IOException|RuntimeException ex)
        {
            throw ex;
        }
        catch (Throwable ex)
        {
            throw new IOException(ex);
        }
    }

    public String getUrl()
    {
        return url;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this==o)
        {
            return true;
        }
        if (!(o instanceof Repository))
        {
            return false;
        }
        Repository that=(Repository) o;
        return Objects.equals(getUrl(),that.getUrl());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getUrl());
    }

    @Override
    public String toString()
    {
        return "Repository{"+"url='"+url+'\''+'}';
    }
}