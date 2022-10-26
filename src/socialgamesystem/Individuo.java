package socialgamesystem;

import java.util.*; //Importa le librerie di Random e di Map e di ArrayList

public class Individuo {

    public static final int MESSAGES = 10; //Numero che rappresenta l'alfabeto degli individui (scambiano MESSAGES da 1 a 10)
    public static final int MAXCOMFORT = 100;
    public double comfort; //Il comfort rappresenta lo stato di ogni singolo individuo e varia da 0 (<= morto) a MAXCOMFORT (riproduzione a 3/4)
    static final int dimNeighborhood = 16; //Definisce la grandezza dell'array delle conoscenze
    ArrayList<Member> neighborhood = new ArrayList<Member>(); //ArrayList delle conoscenze
    Random rand; //Inizializza oggetto per la generazione dei numeri random
    public int first_message; //Dichiarazione primo messaggio da inviare quando due oggetti non si conoscono (è il migliore secondo l'individuo)
    private Game game;
    private int x = (int) this.comfort; //variabìle d'appoggio per calcolare la variazione del benessere
    public int generation; //generazione dell'individuo

    protected float timerCurrent = 0f;
    protected float timerTotal = 10f;

    public LinkedList<Member> daRimuovere = new LinkedList<Member>(); //Lista di elementi da rimuovere dal vicinato

    HashMap<Integer, Integer> input_mod_stato;//Modifica lo stato a seconda della bontà dell'input
    //Dizionari che a seconda dell'input danno una risposta
    HashMap<Integer, Integer> stato_output_diminuito;//Se con l'input ricevuto il comfort è diminuito usa questo
    HashMap<Integer, Integer> stato_output_aumentato;//Altrimenti usa quest'altro

    public Individuo(Game game) {
        /* Costruttore, vengono inizializzati i parametri dell'individuo e altri attributi utili */
        this.comfort = 50; //Comfort iniziale dell'individuo
        this.game = game;
        this.rand = game.rand;
        this.first_message = 0;
        input_mod_stato = new HashMap<Integer, Integer>();
        stato_output_diminuito = new HashMap<Integer, Integer>();
        stato_output_aumentato = new HashMap<Integer, Integer>();
        this.buildStrategy();
    }

    public void buildStrategy() {
        /* Costruisce la strategia dell'indiviudo attraverso la costruzione
           dei tre dizionari propri di ogni individuo:
            1) il dizionario per la modifica del comfort in base all'input
            2) il dizionario per le risposte nel caso in cui il benessere sia diminuito
            3) il dizionario per le risposte nel caso in cui il benessere sia aumentato
           Genera inoltre casualmente l'età a cui morirà
         */
        //Costruzione dizionario 1
        int max_app = -5; //Dichiarazione e inizializzazione variabile per calcolare miglior messaggio da comunicare inizialmente
        for (int i = 1; i <= MESSAGES; i++) {
            int k = rand.nextInt(11) - 5;
            input_mod_stato.put(i, k); //Chiave = input i, valore = numero tra -5 e 5 che incrementa/decrementa lo stato
            if (input_mod_stato.get(i) > max_app) { //Se il nuovo valore inserito è migliore di max_app
                max_app = input_mod_stato.get(i);//lo si sostituisce
                first_message = i;
            }
        }
        //Costruzione dizionario 2
        for (int i = 1; i <= MESSAGES; i++) {
            stato_output_diminuito.put(i, random(MESSAGES)); //Chiave = messaggio ricevuto, valore = messaggio output (casuale tra 1 e 10)
        }
        //Costruzione dizionario 3
        for (int i = 1; i <= MESSAGES; i++) {
            stato_output_aumentato.put(i, random(MESSAGES)); //Chiave = messaggio ricevuto, valore = messaggio output (casuale tra 1 e 10)
        }
    }

    public int random(int x) {
        /* Genera un numero da 1 a x (compreso) */
        return rand.nextInt(x) + 1;
    }

    private void comfortModify(int input, Member m) {
        /* Modifica il comfort a seconda dell'input ricevuto da un altro giocatore
            prendendo il valore associato all'input dal dizionario */
        if (input < 1 || input > 10)
            input = random(10); //Se l'input dovesse essere illegale se ne genera uno nuovo
        m.comfort += input_mod_stato.get(input);
    }

    public void getOutput(int input, Member m1, Member m2) {
        /* Gestisce lo scambio di messaggi tra i due membri */
        x = (int) this.comfort;
        comfortModify(input, m1); //Modifica del comfort
        if (this.comfort < x) { //Se il comfort attuale è minore del precedente
            comfortModify(-(manageCommunications(m1, m2)), m1);
            if (comfort != 0) 
                comfortModify(stato_output_diminuito.get(input), m2); //Risposta
        } else {
            if (manageCommunications(m1, m2) == -30)//I due non si conoscono
                addFriends(m2, m1); //E quindi si aggiungono nei rispettivi array delle amicizie
            else { 
                comfortModify(manageCommunications(m1, m2), m1);
                comfortModify(stato_output_aumentato.get(input), m2);
            }
        }
    }
    
    public void buildSon(int x, int y) {
        /* Crea il figlio */
        int z = generation + 1;
        Member m = new Member(x, y, z, this.timerTotal, game); //Richiama il costruttore
        //Copia dei tre dizionari del padre
        m.input_mod_stato = this.input_mod_stato;
        m.stato_output_aumentato = this.stato_output_aumentato;
        m.stato_output_diminuito = this.stato_output_diminuito;
        this.game.toAppend.add(m); //Inserito nella lista di individui da aggiungere alla prossima iterazione
    }

    public int manageCommunications(Member m1, Member m2) {
        /* Metodo per la gestione della comunicazione tra due individui */
        for (int i = 0; i < m1.neighborhood.size(); i++) {
            if (m1.neighborhood.get(i) == m2) { //Se m2 è già nell'array dei conoscenti di m1
                if (i < dimNeighborhood / 4) //Amico con influenza altissima sull'umore
                    return random(2) + 11; //La felicità nel comunicare con l'altro dipende comunque dall'umore di m1
                if (i < dimNeighborhood / 2) //Amico con influenza alta sull'umore
                    return random(2) + 7;
                if (i < (3 * dimNeighborhood) / 4) //Amico con influenza bassa sull'umore
                    return random(2) + 3;
                if (i < dimNeighborhood) //Conoscente con influenza quasi nulla sull'umore
                    return random(2);
            }
        }
        if (checkFriends(m1, m2)){ //Controlla se entrambi gli oggetti hanno almeno un posto per un amico
            if (this.comfort < x) //Se ha diminuito il comfort  di m1
                return 0; //Non stringono amicizia
            else {
                addFriends(m1, m2); //Se c'è spazio si aggiunge un elemento
                return -30; //ritorno codice di amici aggiunti
            }
        }
        return 0; //Almeno uno dei due vettori è pieno e non si può stringere una nuova amicizia
    }

    private void addFriends(Member m1, Member m2) {
        /* Aggiunge un amico dopo aver verificato la disponibilità */
        if (m1.neighborhood.size() < dimNeighborhood && m2.neighborhood.size() < dimNeighborhood) {
            m1.neighborhood.add(m2);
            m2.neighborhood.add(m1);
        }
    }

    private boolean checkFriends(Member m1, Member m2) {
        /* Controlla se i vettori di entrambi gli individui hanno un posto */
        return m1.neighborhood.size() < dimNeighborhood && m2.neighborhood.size() < dimNeighborhood;
    }
}
