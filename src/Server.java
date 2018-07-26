import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class Server implements Newsinterface{
    private HashMap<Integer, Noticia> news;
    private HashMap<String, Integer> address;

    public static void main(String[] args) {
        try {
            int porta = Integer.parseInt(args[0]); // Recebe com parâmetro o número da porta que o servidor irá escutar
            LocateRegistry.createRegistry(porta).bind("Chibata", UnicastRemoteObject.exportObject(new Server(), porta));
        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }
    }


    public Server() throws Exception{
        news = new HashMap<>();
        address = new HashMap<>();
        /*
            Leitura do arquivo de endereços de servidores
         */
        Files.lines(Paths.get("routes.txt")).forEach(linha -> {
            String[] info = linha.split(":");
            address.put(info[0], Integer.parseInt(info[1]));
        });

        new Thread(()->{
            FileTime altered = null;
            while(true){
                try {
                    if (altered == null) {
                        Files.lines(Paths.get("news.txt")).forEach(linha -> {
                            String[] campos = linha.split(",");
                            Noticia dados = new Noticia(campos[0], Float.parseFloat(campos[1]));
                            int codigo = dados.info.hashCode();

                            if (!news.containsKey(codigo) || news.get(codigo).nota != dados.nota)
                                news.put(codigo, dados);
                        });

                        altered = Files.getLastModifiedTime(Paths.get("news.txt"));
                    } else if (altered.compareTo(Files.getLastModifiedTime(Paths.get("news.txt"))) != 0)
                        altered = null;
                } catch (Exception e) {
                    System.err.println(e);
                }

                /*
                 * Verifica se exitem notícias sem classificação e às classifica
                 */
                for (int codigo : news.keySet()) {
                    if (!news.get(codigo).classificado) {
                        for (String endereco : address.keySet()) {
                            try {
                                Newsinterface noticia = (Newsinterface) LocateRegistry.getRegistry(endereco, address.get(endereco)).lookup("Chibata");
                                noticia.classify(codigo);
                            } catch (Exception e) {
                                System.err.println("Não foi possivel se conectar ao servidor: " + endereco + ":" + address.get(endereco));
                            }
                        }
                    }
                }

            }
        }).start();


    }

    /*
        Retorna avaliação da notícia para determinado código da hash
     */
    @Override
    public float point(int code) throws RemoteException {
        if (news.containsKey(code)) {
            return news.get(code).nota;
        }
        throw new RemoteException();
    }

    @Override
    public void classify(int code) throws RemoteException {
        if (news.containsKey(code)) {
            int resultado = 0;
            int votos = 0;

            // Pergunta aos servers a respeito da noticia e se acreditam que seja fake news ou não
            for (String endereco : address.keySet()) {
                try {
                    Newsinterface noticia = (Newsinterface) LocateRegistry.getRegistry(endereco, address.get(endereco)).lookup("Chibata");
                    resultado += noticia.point(code) < 3 ? -1 : 1;
                    votos++;
                } catch (Exception e) {
                    System.err.println("Não foi possível se conectar ao servidor " + endereco + ":" + address.get(endereco));
                }
            }

            // Altera a classificação da noticia em função da MAIORIA dos servidores envolvidos
            if (votos > address.size() / 2) {
                news.get(code).classificado = true;

                // Notifica as noticias que são fake
                if (resultado < 0 && !news.get(code).notificada) {
                    System.out.println((news.get(code).info+"\n"));
                    news.get(code).notificada = true;
                    /*try {
                        Files.write(Paths.get("notificacoes.txt"), (news.get(code).info+"\n").getBytes(), StandardOpenOption.APPEND);
                        news.get(code).notificada = true;
                    }catch (Exception e) {
                        System.err.println("Erro ao escrever no arquivo de notificações");
                    }*/
                }
            }
        }
    }
}
