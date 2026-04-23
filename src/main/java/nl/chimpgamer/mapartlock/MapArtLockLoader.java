package nl.chimpgamer.mapartlock;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.util.ArrayList;

class MapArtLockLoader implements PluginLoader {

    @Override
    public void classloader(final PluginClasspathBuilder classpathBuilder) {
        var dependencies = new ArrayList<String>() {{
            add("org.jetbrains.kotlin:kotlin-stdlib:2.3.21");
            add("org.incendo:cloud-core:2.0.0");
            add("org.incendo:cloud-paper:2.0.0-beta.14");
            add("org.incendo:cloud-minecraft-extras:2.0.0-beta.14");
            add("org.incendo:cloud-kotlin-coroutines:2.0.0");
            add("dev.dejvokep:boosted-yaml:1.3.7");
        }};

        var mavenLibraryResolver = new MavenLibraryResolver();
        dependencies.forEach(dependency -> mavenLibraryResolver.addDependency(new Dependency(new DefaultArtifact(dependency), null)));
        mavenLibraryResolver.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());

        classpathBuilder.addLibrary(mavenLibraryResolver);
    }
}
