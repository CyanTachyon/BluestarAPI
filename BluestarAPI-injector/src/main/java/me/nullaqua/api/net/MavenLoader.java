package me.nullaqua.api.net;

import me.nullaqua.api.io.IOAccessor;
import me.nullaqua.api.reflect.URLClassLoaderAccessor;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParseException;
import java.util.*;

@SuppressWarnings("UnusedReturnValue")
public class MavenLoader extends AbstractXmlParser
{
    private final static URLClassLoaderAccessor loaderAccessor;

    static
    {
        if (MavenLoader.class.getClassLoader() instanceof URLClassLoader)
        {
            loaderAccessor=new URLClassLoaderAccessor((URLClassLoader) MavenLoader.class.getClassLoader());
        }
        else
        {
            URLClassLoader loader=new URLClassLoader(new URL[0],MavenLoader.class.getClassLoader());
            loaderAccessor=new URLClassLoaderAccessor(loader);
        }
    }

    private static final Set<MavenPacket> injectedDependencies=new HashSet<>();
    private static final Set<MavenPacket> downloadedDependencies=new HashSet<>();
    private final Set<Repository> repositories=new HashSet<>();
    private final File baseDir;
    private MavenPacket.MavenPacketScope[] dependencyScopes={MavenPacket.MavenPacketScope.RUNTIME,
                                                             MavenPacket.MavenPacketScope.COMPILE};
    private boolean ignoreOptional=true;
    private boolean ignoreException=false;
    private boolean isTransitive=true;

    public static void loadJar(File jarFile) throws IOException
    {
        loadJar(jarFile,null);
    }

    public static void loadJar(File jarFile,URLClassLoader classLoader) throws IOException
    {
        URLClassLoaderAccessor accessor;
        if (classLoader==null) accessor=loaderAccessor;
        else accessor=new URLClassLoaderAccessor(classLoader);
        try
        {
            accessor.addURL(jarFile.toURI().toURL());
        }
        catch (Throwable e)
        {
            throw new IOException(e);
        }
    }

    public MavenLoader(@Nullable File baseDir)
    {
        this.baseDir=baseDir;
    }

    public void injectClasspath(Set<MavenPacket> dependencies) throws IOException
    {
        injectClasspath(dependencies,null);
    }

    public void injectClasspath(Set<MavenPacket> dependencies,URLClassLoader loader) throws IOException
    {
        for (MavenPacket dep: dependencies)
        {
            if (injectedDependencies.contains(dep))
            {
                continue;
            }
            File file=dep.findFile(baseDir,"jar");
            if (file.exists())
            {
                loadJar(file,loader);
                injectedDependencies.add(dep);
            }
            else
            {
                try
                {
                    loadDependency(repositories,dep);
                    injectClasspath(Collections.singleton(dep),loader);
                }
                catch (IOException e)
                {
                    throw new IllegalStateException("Unable to load dependency: "+dep,e);
                }
            }
        }
    }

    /**
     * 下载 pom 中指定的所有依赖项
     */
    public Set<MavenPacket> loadDependencyFromInputStream(InputStream pom,MavenPacket.MavenPacketScope... scopes) throws IOException
    {
        try
        {
            DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/validation",false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar",false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
            DocumentBuilder builder=factory.newDocumentBuilder();
            Document xml=builder.parse(pom);
            return loadDependencyFromPom(xml,scopes);
        }
        catch (ParserConfigurationException ex)
        {
            throw new IOException("Unable to load pom.xml parser",ex);
        }
        catch (SAXException ex)
        {
            throw new IOException("Unable to parse pom.xml",ex);
        }
    }

    /**
     * 下载 pom 中指定的所有依赖项
     */
    public Set<MavenPacket> loadDependencyFromPom(Document pom,MavenPacket.MavenPacketScope... scopes) throws IOException
    {
        List<MavenPacket> dependencies=new ArrayList<>();
        Set<MavenPacket.MavenPacketScope> scopeSet=new HashSet<>(Arrays.asList(scopes));
        NodeList nodes=pom.getDocumentElement().getChildNodes();
        List<Repository> repos=new ArrayList<>(repositories);
        if (repos.isEmpty())
        {
            repos.add(new Repository());
        }
        try
        {
            for (int i=0;i<nodes.getLength();++i)
            {
                Node node=nodes.item(i);
                if (node.getNodeName().equals("repositories"))
                {
                    nodes=((Element) node).getElementsByTagName("repository");
                    for (i=0;i<nodes.getLength();++i)
                    {
                        Element e=(Element) nodes.item(i);
                        repos.add(new Repository(e));
                    }
                    break;
                }
            }
        }
        catch (ParseException ex)
        {
            throw new IOException("Unable to parse repositories",ex);
        }
        if (isTransitive)
        {
            nodes=pom.getElementsByTagName("dependency");
            try
            {
                for (int i=0;i<nodes.getLength();++i)
                {
                    if (ignoreOptional&&find("optional",(Element) nodes.item(i),"false").equals("true"))
                    {
                        continue;
                    }
                    MavenPacket dep=new MavenPacket((Element) nodes.item(i));
                    if (scopeSet.contains(dep.getScope()))
                    {
                        dependencies.add(dep);
                    }
                }
            }
            catch (ParseException ex)
            {
                if (!ignoreException)
                {
                    throw new IOException("Unable to parse dependencies",ex);
                }
            }
        }
        return loadDependency(repos,dependencies);
    }

    /**
     * 下载一个依赖项列表以及它们的所有依赖项，并将它们存储在 {@link MavenLoader#baseDir} 中。
     */
    public Set<MavenPacket> loadDependency(List<Repository> repositories,List<MavenPacket> dependencies) throws IOException
    {
        createBaseDir();
        Set<MavenPacket> downloaded=new HashSet<>();
        for (MavenPacket dep: dependencies)
        {
            downloaded.addAll(loadDependency(repositories,dep));
        }
        return downloaded;
    }

    /**
     * 确保 {@link MavenLoader#baseDir} 存在
     */
    private void createBaseDir()
    {
        baseDir.mkdirs();
    }

    public Set<MavenPacket> loadDependency(Collection<Repository> repositories,MavenPacket dependency) throws IOException
    {
        // 未指定仓库
        if (repositories.isEmpty())
        {
            throw new IllegalArgumentException("No repositories specified");
        }
        // 如果已经下载过了，就直接返回
        if (downloadedDependencies.contains(dependency))
        {
            Set<MavenPacket> singleton=new HashSet<>();
            singleton.add(dependency);
            return singleton;
        }
        // 获取依赖项的 pom 文件和 jar 文件
        File pom=dependency.findFile(baseDir,"pom");
        File pom1=new File(pom.getPath()+".sha1");
        File jar=dependency.findFile(baseDir,"jar");
        File jar1=new File(jar.getPath()+".sha1");
        Set<MavenPacket> downloaded=new HashSet<>();
        downloaded.add(dependency);
        // 检查文件的完整性
        if (IOAccessor.validation(pom,pom1)&&IOAccessor.validation(jar,jar1))
        {
            // 加载依赖项
            downloadedDependencies.add(dependency);
            if (pom.exists())
            {
                downloaded.addAll(loadDependencyFromInputStream(pom.toURI().toURL().openStream()));
            }
            return downloaded;
        }
        // 创建所在目录
        pom.getParentFile().mkdirs();
        // 下载文件
        IOException e=null;
        for (Repository repo: repositories)
        {
            try
            {
                System.err.println("tryDownload: "+repo.getUrl());
                repo.downloadFile(dependency,pom);
                repo.downloadFile(dependency,jar);
                e=null;
                break;
            }
            catch (Exception ex)
            {
                e=new IOException(String.format("Unable to find download for %s (%s)",dependency,repo.getUrl()),ex);
            }
        }
        // 如果存在异常，则抛出
        if (e!=null)
        {
            throw e;
        }
        return downloaded;
    }

    public Set<MavenPacket> loadDependencyFromInputStream(InputStream pom) throws IOException
    {
        return loadDependencyFromInputStream(pom,dependencyScopes);
    }

    public void addRepository(Repository repository)
    {
        repositories.add(repository);
    }

    public File getBaseDir()
    {
        return baseDir;
    }

    public MavenPacket.MavenPacketScope[] getDependencyScopes()
    {
        return dependencyScopes;
    }

    public MavenLoader setDependencyScopes(MavenPacket.MavenPacketScope[] dependencyScopes)
    {
        this.dependencyScopes=dependencyScopes;
        return this;
    }

    public Set<MavenPacket> getInjectedDependencies()
    {
        return injectedDependencies;
    }

    public Set<Repository> getRepositories()
    {
        return repositories;
    }

    public boolean isIgnoreOptional()
    {
        return ignoreOptional;
    }

    public MavenLoader setIgnoreOptional(boolean ignoreOptional)
    {
        this.ignoreOptional=ignoreOptional;
        return this;
    }

    public MavenLoader setIgnoreException(boolean ignoreException)
    {
        this.ignoreException=ignoreException;
        return this;
    }

    public boolean isTransitive()
    {
        return isTransitive;
    }

    public void setTransitive(boolean transitive)
    {
        isTransitive=transitive;
    }
}