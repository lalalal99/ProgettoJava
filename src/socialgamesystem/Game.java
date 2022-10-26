package socialgamesystem;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.io.BufferedReader;
import java.util.LinkedList;
import java.util.Random;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

public class Game implements Runnable {

    private Display display;
    public int width, height;
    public String title;
    public double delta;
    private final int maxGeneration = 1;

    public boolean running = false;
    private Thread thread;

    private BufferStrategy bs;
    private Graphics g;

    private KeyManager keymanager;
    private MouseManager mousemanager;
    
    public Random rand = new Random();
    
    private int n = 50; //Numero di elementi iniziali nell'arena
    private LinkedList<Member> members = new LinkedList<Member>();

    public boolean isMenuDrawn = false;
    public Member toDraw = null;    
    
    public static String [] names = new String [180];
    
    public LinkedList<Member> toAppend = new LinkedList<Member>();
    public LinkedList<Member> toRemove = new LinkedList<Member>(); //Lista con i membri da rimuovere dalla lista globale dei membri
    
    public LinkedList<Member> maxGenList = new LinkedList<Member>();
    
    public boolean freeze = false; //Variabile utile a freezare la grafica
    

    public Game(String title, int width, int height) {
        /* Costruttore */
        this.width = width;
        this.height = height;
        this.title = title;
        this.keymanager = new KeyManager();
        this.mousemanager = new MouseManager();
    }

    private void init() {
        display = new Display(title, width, height);
        display.getFrame().addKeyListener(keymanager);
        display.getFrame().addMouseListener(mousemanager);
        display.getFrame().addMouseMotionListener(mousemanager);
        display.getCanvas().addMouseListener(mousemanager);
        display.getCanvas().addMouseMotionListener(mousemanager);
        
        initNames();
        
        for (int i = 0; i < n; i++) 
            this.members.add(new Member(this));
    }

    private void update() {
        keymanager.update();
        mousemanager.update();
        
        for (Member membro : this.members)
            membro.update();
        
        members.addAll(toAppend);
        toAppend.clear(); 
        
        members.removeAll(toRemove);
        toRemove.clear();
        
        if (this.toDraw == null)
            this.isMenuDrawn = false;
        
        if(findMaxGeneration() == maxGeneration + 1){
            this.freeze = true;
            emerge();
        }
        
        if (this.keymanager.esc) 
            for (Member membro : this.members)
                membro.graph = true;
        else {
            for (Member membro : this.members)
                membro.graph = false;
        }
        
        buildMaxGenList();
        for (Member membro : this.maxGenList)
            membro.isMaxGen = true;
    }
    
    private void buildMaxGenList(){
        /* Costruisce la lista degli individui alla generazione massima */
        maxGenList.clear();
        int maxG = findMaxGeneration();
        for(Member m : this.members){
            m.isMaxGen = false;
            if(m.generation == maxG)
                maxGenList.add(m);
        }
    }
    
    private int findMaxGeneration(){
        /* Trova la generazione massima geerazione tra quelle presenti */
        int max = 0;
        for(Member m : this.members)
            if (m.generation > max)
                max = m.generation;
        return max;
    }
    
    public LinkedList<Member> getMembers() {
        return members;
    }

    private void render() {
        /* Renderizza gli oggetti permettendone la visualizzazione a video */
        bs = display.getCanvas().getBufferStrategy();
        if (bs == null){
            display.getCanvas().createBufferStrategy(3);
            return;
        }
        g = bs.getDrawGraphics();
        //Clear Screen
        g.clearRect(0, 0, width, height);

        for (Member membro : this.members)
            membro.render(g);
        
        g.drawString("Popolazione Corrente: " + this.members.size(), 10 , 10);
        
        if (this.isMenuDrawn && this.toDraw != null)
            drawMenu(g, toDraw);
        
        bs.show();
        g.dispose();
    }

    public void drawMenu(Graphics g, Member m) {
        /*Disegna il menù quando si clicca sull'individuo */
        int menuSize = 200;
        int dist = 1;
        int x = this.getDisplay().getCanvas().getWidth() - menuSize;
        int y = 0;

        //menu
        g.setColor(Color.darkGray);
        g.fillRect(x, y, menuSize, this.getDisplay().getCanvas().getHeight() / 7);

        //scritte
        g.setColor(Color.white);
        g.drawString("Nome: " + m.getName(), this.getDisplay().getCanvas().getWidth() - (menuSize - 5), 20 * dist++);
        g.drawString("Benessere: " + (int) m.getBenessere(), this.getDisplay().getCanvas().getWidth() - (menuSize - 5), 20 * dist++);
        g.drawString("Generazione: " + m.generation, this.getDisplay().getCanvas().getWidth() - (menuSize - 5), 20 * dist++);        
    }
    
    public void emerge(){
        /* Funzione di emersione del linguaggio: scrive i dizionari su un file */
        LinkedList<Member> massimi = new LinkedList<>();
                
        for(Member m: members)
            if(m.generation == maxGeneration)
                massimi.add(m);
        try{
            PrintWriter writer = new PrintWriter("dizionariEmersi.txt", "UTF-8");
            for(Member m: massimi){
                writer.println(m.getName() + ":");
                writer.println("Modifica stato:    " + m.input_mod_stato);
                writer.println("Output +benessere: " + m.stato_output_aumentato);
                writer.println("Output -benessere: " + m.stato_output_diminuito);
                writer.println();
            }
            writer.println("Linguaggio ideale:");
            writer.println("Modifica stato:    " + mediana(massimi, 1));
            writer.println("Output +benessere: " + mediana(massimi, 2));
            writer.println("Output -benessere: " + mediana(massimi, 3));
            writer.close();
        }catch(Exception e){
            System.out.println("Errore");
        }
    }
    
    private HashMap<Integer, Integer> mediana(LinkedList<Member> massimi, int x){
        /* Attraverso il calcolo della mediana permette l'individuazione di un
            linguaggio ideale. A parità di frequenza viene preso il primo valore
            (quello più basso)
        */
        int [] v = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        HashMap<Integer, Integer> dap = new HashMap<Integer, Integer>();
        switch(x){
            case 1:
                for (int i = 1; i < v.length; i++){
                    for (Member m : massimi){
                        v[m.input_mod_stato.get(i) + 5]++;
                    }
                    dap.put(i, max(v) - 5);
                    clearArray(v);
                }
                break;
            case 2:
                for (int i = 1; i < v.length; i++){
                    for(Member m : massimi){
                        v[m.stato_output_aumentato.get(i) - 1]++;
                    }
                    dap.put(i, max(v) + 1);
                    clearArray(v);
                }
                break;
            case 3:
                for (int i = 1; i < v.length; i++){
                    for(Member m : massimi){
                        v[m.stato_output_diminuito.get(i) - 1]++;
                    }
                    dap.put(i, max(v) + 1);
                    clearArray(v);
                }
                break;
        }
        return dap;
    }
    
    private int max(int [] v){
        /* Restituisce l'indice con frequenza massima nell'array */
        int max = 0;
        for(int i = 1; i < v.length; i++)
            if(v[i] > v[max])
                max = i;
        return max;
    }
    
    private void clearArray(int [] v){
        /* Riporta a 0 gli elementi dell'array */
        for(int i = 0; i < v.length; i++)
            v[i] = 0;
    }
    
    public void run() {
        init();

        int fps = 60;
        double timePerTick = 1000000000 / fps;
        this.delta = 0;
        long now;
        long lastTime = System.nanoTime();

        while (running) {
            now = System.nanoTime();
            this.delta += (now - lastTime) / timePerTick;
            lastTime = now;

            if (this.delta >= 1) {
                update();
                render();
                this.delta--;
            }
        }
        stop();
    }

    public KeyManager getKeyManager() {
        return keymanager;
    }

    public MouseManager getMouseManager() {
        return mousemanager;
    }

    public Display getDisplay() {
        return display;
    }

    public Graphics getGraphics() {
        return g;
    }

    public synchronized void start(){
        if (running)
            return;
        running = true;
        thread = new Thread(this);
        thread.start();

    }
    
    private void initNames(){
        /* Apre il file leggendolo e memorizzando in names i nomi letti da file */
        String fn = "src" + File.separatorChar + "socialgamesystem" + File.separator + "nomi.txt";
        int i = 0;
        try{
            BufferedReader br = new BufferedReader(new FileReader(fn));
            String line = br.readLine();
            while(line != null){
                names[i++] = line;
                line = br.readLine();
            }
        }catch(Exception e){
            System.out.println("Errore");
        }
    }

    public synchronized void stop() {
        if (!running)
            return;
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
