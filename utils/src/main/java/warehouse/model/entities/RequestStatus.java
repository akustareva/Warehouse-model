package warehouse.model.entities;

public enum RequestStatus {
    COMPLETED ("complete"),
    IN_PROGRESS ("in progress"),
    CANCELED ("canceled");

    private String text;
    RequestStatus(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
