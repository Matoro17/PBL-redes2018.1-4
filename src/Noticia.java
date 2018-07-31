/*
    Classe para armazenar informações da notícia e sua nota e se foi classifica pelos servidores e já fora notificada que é uma fake news
*/
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
