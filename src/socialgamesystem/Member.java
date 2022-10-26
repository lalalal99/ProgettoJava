package socialgamesystem;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.concurrent.ThreadLocalRandom;

public class Member extends Individuo{
	
    private Game game; //Variabile per accedere alla classe game

    private int x; //Posizione effettiva del quadrato
    private int y; 
    private int width;
    private int height;
    private Color color;
    private int animationCounter; //Contatore dei frame per le animazioni
    public boolean clicked = false;
    public boolean alive = true;

    private int x0; //Posizione intorno a cui oscilla
    private int y0;

    private int xO; //Posizione di partenza prima dell'incontro
    private int yO;

    private int vel; //Determina la velocità dei movimenti e la frequenza degli incontri

    private boolean meeting;
    private boolean goingBack;
    private int waitTime; //Tempo di attesa prima di tornare indietro

    private Member other;
    private String name;

    public boolean graph; //Determina se si deve disegnare il grafo delle amicizie

    private Color graphColor; //Colore delle linee del grafo

    public boolean isMaxGen = false; //Indica se l'individuo è a generazione massima


	
    public Member(Game game){
        /* Costruttore per i padri che usa quello di individuo e inizializza altri parametri per l'interfaccia */
        super(game);
        this.x = ThreadLocalRandom.current().nextInt(1, game.getDisplay().getCanvas().getWidth() - this.width) + 1;
        this.y = ThreadLocalRandom.current().nextInt(1, game.getDisplay().getCanvas().getHeight() - this.height) + 1;
        init(game);
        this.generation = 0;
    }

    public Member(int x, int y, int z, float Timer, Game game) {
        /* Costruttore utilizzato per i figli */
        super(game);        
        this.x = x;
        this.y = y;
        init(game);
        this.generation = z;
        super.timerTotal = Timer;
    }

    private void init(Game game) {
        /* Inizializza altri parametri per l'interfaccia */
        this.game = game;
        this.animationCounter = 0;
        this.color = Color.green;
        this.graphColor = new Color(this.game.rand.nextInt(50) + 127, this.game.rand.nextInt(50) + 127, this.game.rand.nextInt(50) + 127);
        this.width = 15;
        this.height = 15;
        this.x0 = this.x;
        this.y0 = this.y;
        this.xO = this.x0;
        this.yO = this.y0;
        this.meeting = false;
        this.goingBack = false;
        this.waitTime = 0;
        this.vel = 10; //Modificare per modificare anche velocità del gioco
        this.name = setName(); //Da il nome al giocatore
        this.timerTotal = (this.game.rand.nextInt(400) + 300) / proportion(vel, 0, 10, 1, 2); //Sfrutta proporzione per stabilire la velocità degli incontri
    }
        
    private String setName(){
        /* Fuznzione che setta il nome all'individuo */
        int nr = rand.nextInt(180); //Sceglie un nome dei 180 nel file
        return game.names[nr];
    }
	
    public void update() {
        /* Eseguita ad ogni frame aggiorna i valori del singolo membro */
        if (!this.graph && !this.game.freeze) {	//Se il gioco non è freezato
            if (this.alive) { //E l'individuo è vivo
                this.color = this.fade();	
                
                move();
                
                if (this.meeting) {
                    this.goingBack = false;
                    this.approach(this.other);
                }
                else if(this.goingBack && this.waitTime == 0)
                        this.goBack(this.other);
                
                if (this.waitTime > 0)
                        this.waitTime--;
                
                animationCounter++;
                
                if (this.animationCounter % 7 == 0 && !this.meeting && game.rand.nextBoolean())
                    this.comfort -= 0.0000001;
                
                timerCurrent += this.game.delta;
                if(timerCurrent >= timerTotal) { //Se il tempo d'attesa è finito
                    this.manageMeeting();
                    timerCurrent -= timerTotal;
                }
                
                this.comfort = Math.max(0, Math.min(this.comfort, MAXCOMFORT)); //Fatto per tenere il comfort nei valori legali
                if(this.comfort <= 0)
                    this.killPlayer(); //Uccide il giocatore
            }
        }
        neighborhood.removeAll(daRimuovere); //Toglie dall'array di vicini tutti quelli in daRimuovere
        daRimuovere.clear(); //Svuota daRimuovere
    }
        
    private void manageMeeting(){
        /* Sceglie gli individui da incontrare in base al numero delle sue conoscenze */
    	if (!this.meeting){ //Se non è in fase di incontro
            int j;
            if (this.neighborhood.size() < dimNeighborhood) { //Sceglie un amico a caso tra quelli presenti nell'arena
                do {
                    j = rand.nextInt(this.game.getMembers().size());
                }while(this.game.getMembers().get(j).isMeeting()); //Se l'altro è in fase di incontro ne sceglie un altro ancora
                this.setMeeting(this.game.getMembers().get(j)); //E si incontrano
            }
            else{ //Se ha già il massimo delle conoscenze allora sceglie un amico da rincontrare
                do {
                    j = rand.nextInt(dimNeighborhood);
                }while(this.game.getMembers().get(j).isMeeting());
                this.setMeeting(this.neighborhood.get(j));
            }
        }
    }
	
    public void killPlayer() {
        /* Uccide l'individuo */
    	this.alive = false;
    	for(Member friend: this.neighborhood) //Rimuove se stesso dagli array degli amici
    		friend.daRimuovere.add(this);
        this.game.toRemove.add(this); //E viene inserito in quelli da rimuovere
    }

    private void approach(Member other) {
        /* L'individuo va verso quello da incontrare e si gestisce il primo incontro */
        if (this.animationCounter % 5 == 0) {
        	
            int diffX = other.x0 - x0;
            int diffY = other.y0 - y0;
            
            float angle = (float) Math.atan2(diffY, diffX);
            
            x0 += this.vel * Math.cos(angle);
            y0 += this.vel * Math.sin(angle);
            
            diffX = x0 - other.x0;
            diffY = y0 - other.y0;
            
            angle = (float) Math.atan2(diffY, diffX);
            
            other.x0 += other.vel * Math.cos(angle);
            other.y0 += other.vel * Math.sin(angle);
            
            if (this.x0 - other.x0 < 5 && this.y0 - other.y0 < 5) { //Se sono abbastanza vicini
                this.waitTime = 70;
                this.meeting = false;
                this.goingBack = true;
                
                if (this.comfort > (MAXCOMFORT/4)*3){ //se il comfort è > 75 crea nuovo figlio
                    this.buildSon(this.x, this.y);
                    this.comfort = (int) MAXCOMFORT/2;
                }
                
                int checker = this.manageCommunications(this, other); //Si effettuano modifiche sul benessere di this , poi andr� in other
                if (checker == -30) ///I due amici si sono aggiunti adesso
                    other.getOutput(this.first_message, other, this);
                else{
                    if(rand.nextInt(2) == 0) //Messaggio negativo
                        other.getOutput(stato_output_diminuito.get(random(10)), other, this);
                    else
                        other.getOutput(stato_output_aumentato.get(random(10)), other, this);
                } 
            }
        }
    }

    private void goBack(Member other) {
        /* Permette all'individuo di tornare alla posizione iniziale */
        if (this.animationCounter % 5 == 0) {
            int diffX = this.xO - this.x0;
            int diffY = this.yO - this.y0;
            
            float angle = (float)Math.atan2(diffY, diffX);
            
            this.x0 += this.vel * Math.cos(angle);
            this.y0 += this.vel * Math.sin(angle);
            
            diffX = other.xO - other.x0;
            diffY = other.yO - other.y0;
            
            angle = (float)Math.atan2(diffY, diffX);
            
            other.x0 += other.vel * Math.cos(angle);
            other.y0 += other.vel * Math.sin(angle);
        }
    }
	
    private void move(){
        /* Fa si che il personaggio "oscilli" */
        if(animationCounter % 7 == 0) {
            this.x = ThreadLocalRandom.current().nextInt(this.x0 - 1, this.x0 + 2);
            this.y = ThreadLocalRandom.current().nextInt(this.y0 - 1, this.y0 + 2);
        }
    }
	
    public Color fade() {
        /* Permette il cambio graduale del colore */
        int proportioned = 1;
        proportioned = (int) this.proportion((int) this.comfort, 0, MAXCOMFORT, 0, 255);
        int r = Math.max(0, Math.min(255 - proportioned, 255));
        int g = Math.max(0, Math.min(0 + proportioned, 255));
        int b = Math.max(0, Math.min(0, 255));		
        return new Color(r, g, b);        
    }
	
    public float proportion (float value, float start1, float stop1, float start2, float stop2) {
        /* Funzione che permette le proporzioni */
        return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    }

    public void render(Graphics g) {
        /* Renderizza gli oggetti permettendone la visualizzazione a video */
        if (this.alive) {
            if (game.getMouseManager().getMouseX() > this.x0 && game.getMouseManager().getMouseX() < this.x0 + this.width &&
                    game.getMouseManager().getMouseY() > this.y0 && game.getMouseManager().getMouseY() < this.y0 + this.height) {
                if (game.getMouseManager().isClicked()) {
                    for (Member membro: this.game.getMembers())
                            membro.clicked = false;
                    this.clicked = true;
                }
            }
            else if(game.getMouseManager().isRightPressed()) {
                for (Member membro: this.game.getMembers())
                    membro.clicked = false;
                this.game.toDraw = null;
            }
            
            if (this.graph) {
                g.setColor(this.graphColor);
                Graphics2D g2 = (Graphics2D) g;
                Stroke oldStroke = g2.getStroke();
                g2.setStroke(new BasicStroke(2));
                
                for(Member m : this.neighborhood)
                    if (m.alive)
                        this.drawLine(m, g);
                
                g2.setStroke(oldStroke);
            }
            
            if (this.clicked) {
                this.highlightFriends(g);	
                this.game.isMenuDrawn = true;
                this.game.toDraw = this;	
                
                g.setColor(new Color(255, 0, 0, 125));
                g.fillOval(this.x - this.width/2 - 1, this.y - this.height/2 - 1, this.width*2, this.height*2);							
            }
            
            if(this.isMaxGen && this.generation != 0)
                g.setColor(color.cyan);
            else
                g.setColor(color);
            
            g.fillRoundRect(x, y, width, height, 2, 2);	
            g.setColor(Color.black);
            g.drawRoundRect(x, y, width, height, 2, 2);
        }
    }
    
    public void drawLine(Member m, Graphics g) {
        /* Disegna la linea da m a g */
        g.drawLine(this.x0 + this.width/2, this.y0 + this.height/2, m.x0 + m.width/2, m.y0 + m.height/2);
    }
		
    public void highlightFriends(Graphics g) {
        /* Evidenzia gli amici del singolo individuo */
        Graphics2D g2 = (Graphics2D) g; //Motore grafico a due dimensioni per disegnare le linee
        Stroke oldStroke = g2.getStroke(); //Valore della linea prima di diventare grassa (cambia spessore linea)
        for (Member friend: this.neighborhood) {
            g.setColor(graphColor);
            g2.setStroke(new BasicStroke(2));
            this.drawLine(friend, g2);
            
            if (friend != null && friend.alive) {
                g2.setColor(Color.red);
                g2.drawRoundRect(friend.x, friend.y, friend.width + 1, friend.height + 1, 2, 2);
                g2.setStroke(oldStroke);
            }
        }
    }
    
    public void setMeeting(Member other) {
        this.meeting = true;
        this.other = other;
    }

    public boolean isMeeting() {
        return meeting;
    }
        
    public String getName(){
        return this.name;
    }
    
    public double getBenessere() {
        return comfort;
    }

    public int getX() {
        return x0;
    }
	
    public int getY() {
        return y0;
    }

    public Color getColor() {
        return color;
    }
}
