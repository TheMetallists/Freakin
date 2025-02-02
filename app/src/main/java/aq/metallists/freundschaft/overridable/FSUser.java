package aq.metallists.freundschaft.overridable;


public class FSUser {
    protected String email="";
    protected String password = "";
    protected String callsign = "";
    protected String decaription = "";
    protected String country = "";
    protected String city = "";

    public FSUser(String _email, String _password){
        this.email = _email;
        this.password = _password;
    }
    
    public String getEmail(){
        return this.email;
    }
    
    public String getPassword(){
        return this.password;
    }

    public String getCallsign(){
        return this.callsign;
    }
    public String getDecaription(){
        return this.decaription;
    }
    public String getCountry(){
        return this.country;
    }
    public String getCity(){
        return this.city;
    }
}
