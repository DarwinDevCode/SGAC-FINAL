public class CredencialesUsuario {
    private String nombreUsuario;
    private String contraseniaUsuario;

    public CredencialesUsuario() {
    }

    public CredencialesUsuario(String nombreUsuario, String contraseniaUsuario) {
        this.nombreUsuario = nombreUsuario;
        this.contraseniaUsuario = contraseniaUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContraseniaUsuario() {
        return contraseniaUsuario;
    }

    public void setContraseniaUsuario(String contraseniaUsuario) {
        this.contraseniaUsuario = contraseniaUsuario;
    }
}