package xyz.emirdev.emirnametags;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class EmirNametagsPluginLoader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("maven-central", "default", "https://repo1.maven.org/maven2/").build());
        resolver.addRepository(new RemoteRepository.Builder("jitpack.io", "default", "https://jitpack.io").build());
        resolver.addDependency(new Dependency(new DefaultArtifact("io.github.revxrsal:lamp.common:4.0.0-rc.9"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("io.github.revxrsal:lamp.bukkit:4.0.0-rc.9"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.github.Carleslc.Simple-YAML:Simple-Yaml:1.8.4"), null));

        classpathBuilder.addLibrary(resolver);
    }
}
