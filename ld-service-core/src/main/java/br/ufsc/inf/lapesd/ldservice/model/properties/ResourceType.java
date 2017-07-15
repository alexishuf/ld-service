package br.ufsc.inf.lapesd.ldservice.model.properties;

import javax.annotation.Nonnull;

/**
 * The selector which has this property selects resources (or lists of) which have the rdf:type
 * specified by getURI(). When this is true for all resources, not just some eventual ones,
 * <code>isCertain() == true</code>.
 *
 * Instances of this class compare equality by value.
 */
public class ResourceType implements SelectorProperty {
    private final boolean certain;
    private final @Nonnull String uri;

    public ResourceType(boolean certain, String uri) {
        this.certain = certain;
        this.uri = uri;
    }

    /**
     * @return true iff all resources of the selector have the type specified by getURI()
     */
    public boolean isCertain() {
        return certain;
    }

    /**
     * The type of (some or all, see isCertain()) resources served by the selector
     *
     * @return The absolute URI of the type.
     */
    public String getURI() {
        return uri;
    }

    @Override
    public String toString() {
        return String.format("ResourceType(%scertain, %s)", isCertain() ? "un" : "", getURI());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceType that = (ResourceType) o;

        return certain == that.certain && uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        int result = (certain ? 1 : 0);
        result = 31 * result + uri.hashCode();
        return result;
    }
}
