public class Noticia {
    String info;
    float nota;
    boolean classificado;
    boolean notificada;

    Noticia(String text, float point){
        this.info = text;
        this.nota = point;
        this.classificado = false;
        this.notificada = false;
    }
}
