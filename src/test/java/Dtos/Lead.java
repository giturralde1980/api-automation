package Dtos;

public class Lead {
    private String lastname;
    private String company;

    public Lead(String lastname, String company)
    {
        this.lastname=lastname;
        this.company=company;
    }

    public String getCompany() {
        return company;
    }

    public String getLastname()
    {
        return this.lastname;
    }
}

