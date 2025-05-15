package app.tracktune.model.author;

public class Author implements Comparable<Author>{
    private final String authorshipName;
    private AuthorStatusEnum status;

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

    @Override
    public int compareTo(Author other) {
        return authorshipName.compareToIgnoreCase(other.authorshipName);
    }

    public AuthorStatusEnum getStatus() {
        return status;
    }

    public void setStatus(AuthorStatusEnum status) {
        this.status = status;
    }
}
