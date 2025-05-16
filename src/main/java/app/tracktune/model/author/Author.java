package app.tracktune.model.author;

public class Author implements Comparable<Author>{
    private final String authorshipName;
    private final AuthorStatusEnum status;

    public Author(String authorshipName) {
        this.authorshipName = authorshipName;
        this.status = AuthorStatusEnum.ACTIVE;
    }
    public Author(String authorshipName, AuthorStatusEnum status) {
        this.authorshipName = authorshipName;
        this.status = status;
    }

    public String getAuthorshipName() {
        return authorshipName;
    }

    public AuthorStatusEnum getStatus() {
        return status;
    }

    @Override
    public int compareTo(Author other) {
        return authorshipName.compareToIgnoreCase(other.authorshipName);
    }
}
