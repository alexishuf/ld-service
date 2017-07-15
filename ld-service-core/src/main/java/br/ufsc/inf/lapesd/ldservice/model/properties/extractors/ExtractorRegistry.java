package br.ufsc.inf.lapesd.ldservice.model.properties.extractors;

import br.ufsc.inf.lapesd.ldservice.model.Selector;
import br.ufsc.inf.lapesd.ldservice.model.properties.SelectorProperty;
import com.google.common.base.Preconditions;
import com.google.common.reflect.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("WeakerAccess")
public class ExtractorRegistry {
    private static final Logger logger;
    public static final ExtractorRegistry instance;

    private Map<Class<? extends SelectorProperty>,
                Map<Class<? extends Selector>, SelectorPropertyExtractor>
            > extractors = new HashMap<>();

    @Nonnull
    public static ExtractorRegistry get() {
        return instance;
    }

    @Nonnull
    public synchronized <T extends Selector> ExtractorRegistry
    register(@Nonnull SelectorPropertyExtractor extractor,
             @Nonnull Class<? extends SelectorProperty> propertyClass,
             @Nonnull Class<? extends Selector> selectorClass) {
        Map<Class<? extends Selector>, SelectorPropertyExtractor> m2;
        m2 = extractors.getOrDefault(propertyClass, null);
        if (m2 == null)
            extractors.put(propertyClass, m2 = new HashMap<>());
        m2.put(selectorClass, extractor);
        return this;
    }

    @Nonnull
    public synchronized ExtractorRegistry
    register(SelectorPropertyExtractor extractor) {
        Class<? extends SelectorPropertyExtractor> clazz = extractor.getClass();
        ExtractsSelectorProperty ann = clazz.getAnnotation(ExtractsSelectorProperty.class);
        Preconditions.checkArgument(ann != null);
        return register(extractor, ann.type(), ann.from());
    }

    @Nullable
    public <P extends SelectorProperty, S extends Selector>
    SelectorPropertyExtractor<P> find(@Nonnull Class<P> exactPropertyClass,
                                         @Nonnull Class<S> selectorClass) {
        Map<Class<? extends Selector>, SelectorPropertyExtractor> m2;
        m2 = extractors.getOrDefault(exactPropertyClass, null);

        Class<? extends Selector> best = Selector.class;
        for (Class<? extends Selector> candidate : m2.keySet()) {
            if (candidate.isAssignableFrom(selectorClass) && best.isAssignableFrom(candidate))
                best = candidate;
        }
        //noinspection unchecked
        return (SelectorPropertyExtractor<P>)m2.getOrDefault(best, null);
    }

    @Nullable
    public <P extends SelectorProperty, T extends Selector>
    Set<P> extract(@Nonnull Class<P> propertyClass, @Nonnull T selector) {
        SelectorPropertyExtractor extractor = find(propertyClass, selector.getClass());
        //noinspection unchecked
        return extractor == null ? null : extractor.extract(selector);
    }

    @Nullable
    public <P extends SelectorProperty, T extends Selector>
    Set<P> extractAndMerge(@Nonnull Class<P> propertyClass, @Nonnull T selector) {
        SelectorPropertyExtractor extractor = find(propertyClass, selector.getClass());
        Set<P> set = Collections.emptySet();
        if (extractor != null) {
            //noinspection unchecked
            set = (Set<P>) extractor.extract(selector);
        }
        Set<P> explicit = selector.getProperties(propertyClass);

        Map<Object, P> map = new HashMap<>();
        Stream.concat(set.stream(), explicit.stream()).forEach(p -> map.put(p.getKey(), p));
        return new HashSet<>(map.values());
    }

    static {
        instance = new ExtractorRegistry();
        logger = LoggerFactory.getLogger(ExtractorRegistry.class);

        try {
            List<Class<? extends SelectorPropertyExtractor>> list;
            //noinspection unchecked
            list = ClassPath.from(Thread.currentThread().getContextClassLoader())
                    .getTopLevelClassesRecursive("br.ufsc.inf.lapesd.ldservice")
                    .stream().map(ClassPath.ClassInfo::load).filter(c ->
                            SelectorPropertyExtractor.class.isAssignableFrom(c)
                                    && c.getAnnotation(ExtractsSelectorProperty.class) != null
                    ).map(c -> (Class<? extends SelectorPropertyExtractor>)c)
                    .collect(Collectors.toList());
            for (Class<? extends SelectorPropertyExtractor> clazz : list)
                try {
                    Constructor<? extends SelectorPropertyExtractor> c = clazz.getConstructor();
                    if (c != null) instance.register(c.newInstance());
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException
                        | InstantiationException e) {
                    logger.error("Could not auto-register scanned extractor {}", clazz, e);
                }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
