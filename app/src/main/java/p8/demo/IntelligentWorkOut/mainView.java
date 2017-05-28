package p8.demo.IntelligentWorkOut;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

import static java.lang.Math.abs;

public class mainView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    // thread utiliser pour animer les zones de depot des diamants
    private boolean paused = true;
    private Thread cv_thread;
    SurfaceHolder holder;

    Paint paint;

    // Declaration des images
    private Bitmap BlueBlock;
    private Bitmap RedBlock;
    private Bitmap win;

    private boolean isWon = false;

    // Declaration des objets Ressources et Context permettant d'acc�der aux ressources de notre application et de les charger
    private Resources mRes;
    private Context mContext;

    // tableau modelisant la PlayerCard du jeu
    static int[][] PlayerCard;

    // tableau de reference du terrain
    static int[][] RefCard;

    // ancres pour pouvoir centrer la PlayerCard du jeu
    Rect TopRect;                   // coordonn�es en Y du point d'ancrage de notre PlayerCard
    Rect BottomRect;

    int evtDownX;
    int evtDownY;

    // coordonn�es en X du point d'ancrage de notre PlayerCard

    // taille de la PlayerCard
    static int MatrixWidth = 5;
    static int MatrixHeight = 5;
    int carteTileSize;
    int refTileSise;
    int Margin;

    // constante modelisant les differentes types de cases
    static final int BlueValue = 0;
    static final int RedValue = 1;

    /**
     * The constructor called from the main JetBoy activity
     *
     * @param context
     * @param attrs
     */
    public mainView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // permet d'ecouter les surfaceChanged, surfaceCreated, surfaceDestroyed
        holder = getHolder();
        holder.addCallback(this);

        // chargement des images
        mContext = context;
        mRes = mContext.getResources();
        BlueBlock = BitmapFactory.decodeResource(mRes, R.drawable.bleu);
        RedBlock = BitmapFactory.decodeResource(mRes, R.drawable.rouge);
        win = BitmapFactory.decodeResource(mRes, R.drawable.bravo);

        // creation du thread
        cv_thread = new Thread(this);

        // initialisation des parmametres du jeu
        initparameters();

        // prise de focus pour gestion des touches
        setFocusable(true);


    }

    // chargement du niveau a partir du tableau de reference du niveau
    private void loadlevel() {
        int x, y;
        Random rn = new Random();
        for (int i = 0; i < MatrixHeight; i++) {
            for (int j = 0; j < MatrixWidth; j++) {
                PlayerCard[j][i] = BlueValue;
                RefCard[j][i] = BlueValue;
            }
        }
        for (int i = 0; i < 9; i++) {
            do {
                x = rn.nextInt(MatrixHeight);
                y = rn.nextInt(MatrixWidth);
            } while (PlayerCard[x][y] == RedValue);
            PlayerCard[x][y] = RedValue;
        }
        for (int i = 0; i < 9; i++) {
            do {
                x = rn.nextInt(MatrixHeight);
                y = rn.nextInt(MatrixWidth);
            } while (RefCard[x][y] == RedValue);
            RefCard[x][y] = RedValue;
        }
    }

    public void reshape() {
        if (getWidth() < getHeight()) {
            TopRect = new Rect(getLeft(), getTop(), getRight(), getHeight() / 3 - 20);
            BottomRect = new Rect(getLeft(), getHeight() / 3, getRight(), getBottom());
            carteTileSize = BottomRect.width() / MatrixWidth;
            refTileSise = TopRect.height() / MatrixHeight;
            Margin = (TopRect.width() - MatrixWidth * refTileSise) / 2;
            TopRect.left += Margin;
            TopRect.right -= Margin;
        } else {
            TopRect = new Rect(getWidth() * 2 / 3 + 20, getTop(), getRight(), getBottom());
            BottomRect = new Rect(getLeft(), getTop(), getWidth() * 2 / 3, getBottom());
            carteTileSize = BottomRect.height() / MatrixHeight;
            refTileSise = TopRect.width() / MatrixWidth;
            Margin = (TopRect.height() - MatrixHeight * refTileSise) / 2;
            TopRect.top += Margin;
            TopRect.bottom -= Margin;
        }
    }

    // initialisation du jeu
    public void initparameters() {
        paint = new Paint();
        paint.setColor(0xff0000);
        paint.setDither(true);
        paint.setColor(0xFFFFFF00);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3);
        paint.setTextAlign(Paint.Align.LEFT);
        PlayerCard = new int[MatrixHeight][MatrixWidth];
        RefCard = new int[MatrixHeight][MatrixWidth];
        loadlevel();
        //recalibrage des dimentions
        reshape();
/*
        if ((cv_thread != null) && (!cv_thread.isAlive())) {
            cv_thread.start();
            Log.e("-FCT-", "cv_thread.start()");
        }
*/
    }

    // dessin des fleches
    private void paintarrow(Canvas canvas) {
    }

    // dessin du gagne si gagne
    private void paintwin(Canvas canvas) {
        canvas.drawBitmap(win, null, new Rect(BottomRect.left,
                BottomRect.top + BottomRect.height()/3,
                BottomRect.right,
                BottomRect.bottom - BottomRect.height()/3), null);
    }

    // dessin de la PlayerCard du jeu
    private void paintcarte(Canvas canvas) {
        for (int i = 0; i < MatrixHeight; i++) {
            for (int j = 0; j < MatrixWidth; j++) {
                Rect rectCarte = new Rect(BottomRect.left + j * carteTileSize,//left
                        BottomRect.top + i * carteTileSize,// top
                        BottomRect.left + j * carteTileSize + carteTileSize, //right
                        BottomRect.top + i * carteTileSize + carteTileSize);//bottom
                switch (PlayerCard[i][j]) {
                    case BlueValue:
                        canvas.drawBitmap(BlueBlock, null, rectCarte, null);
                        break;
                    case RedValue:
                        canvas.drawBitmap(RedBlock, null, rectCarte, null);
                        break;
                }
            }
        }
    }

    // dessin de la refCarte
    private void paintref(Canvas canvas) {
        for (int i = 0; i < MatrixHeight; i++) {
            for (int j = 0; j < MatrixWidth; j++) {
                Rect rectCarte = new Rect(TopRect.left + j * refTileSise, //left
                        TopRect.top + i * refTileSise, // top
                        TopRect.left + j * refTileSise + refTileSise, //right
                        TopRect.top + i * refTileSise + refTileSise); //bottom
                switch (RefCard[i][j]) {
                    case BlueValue:
                        canvas.drawBitmap(BlueBlock, null, rectCarte, null);
                        break;
                    case RedValue:
                        canvas.drawBitmap(RedBlock, null, rectCarte, null);
                        break;
                }
            }
        }
    }

    // permet d'identifier si la partie est gagnee (tous les diamants à leur place)
    private boolean isWon() {
        for (int i = 0; i < MatrixHeight; i++) {
            for (int j = 0; j < MatrixWidth; j++) {
                if (PlayerCard[j][i] != RefCard[j][i])
                    return false;
            }
        }
        return true;
    }

    // dessin du jeu (fond uni, en fonction du jeu gagne ou pas dessin du plateau et du joueur des diamants et des fleches)
    private void nDraw(Canvas canvas) {
        canvas.drawRGB(255, 255, 255);
        paintcarte(canvas);
        paintref(canvas);
        if (isWon) {
            paintwin(canvas);
       }
    }

    // callback sur le cycle de vie de la surfaceview
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("-> FCT <-", "surfaceChanged " + width + " - " + height);
        reshape();
    }

    public void surfaceCreated(SurfaceHolder arg0) {
        Log.i("-> FCT <-", "surfaceCreated");
    }


    public void surfaceDestroyed(SurfaceHolder arg0) {
        Log.i("-> FCT <-", "surfaceDestroyed");
    }

    /**
     * run (run du thread créé)
     * on endort le thread, on modifie le compteur d'animation, on prend la main pour dessiner et on dessine puis on libère le canvas
     */
    public void pause()
    {
        Log.i("-> FCT <-", "pause");
        paused = true;
        synchronized (cv_thread) {
            while (paused) {
                try {
                    cv_thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
            cv_thread = null;
        }
    }

    public void resume() {
        Log.i("-> FCT <-", "resume");
        if (cv_thread == null) {
            cv_thread = new Thread(this);
            Log.i("-> FCT <-", "thread create and start");
        }
        cv_thread.start();

        paused = false;
        synchronized (cv_thread) {
            cv_thread.notifyAll();
        }
    }

    public void run() {
        Log.i("-> FCT <-", "run");
        while (!paused)
        {
            synchronized (cv_thread){

            }
            if (!holder.getSurface().isValid())
                continue;
            Canvas c = holder.lockCanvas(null);
            nDraw(c);
            holder.unlockCanvasAndPost(c);
            try {
                cv_thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e("-> RUN <-", "PB DANS RUN");
            }
        }
    }

/*
    public void run() {
        while (in) {
            if (!holder.getSurface().isValid())
                continue;
            Canvas c = holder.lockCanvas(null);
            nDraw(c);
            holder.unlockCanvasAndPost(c);
            try {
                cv_thread.sleep(40);
            } catch (InterruptedException e) {
                Log.e("-> RUN <-", "PB DANS RUN");
            }
        }
    }
*/

    // fonction permettant de recuperer les evenements tactiles
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction(), actionX = (int) event.getX(), actionY = (int) event.getY();
        if (BottomRect.contains(actionX, actionY)) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if(isWon){
                        loadlevel();
                        isWon = false;
                    }
                    evtDownX = actionX; evtDownY = actionY;
                    //Log.i("-> FCT <-", "onTouchEvent down: x = " + evtDownX + " y = " + evtDownY);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    //Log.i("-> FCT <-", "onTouchEvent move: x = " + actionX + " y = " + actionY);
                    return true;
                case MotionEvent.ACTION_UP:
                    int deltaX = (actionX - evtDownX), deltaY = (actionY - evtDownY);
                    //Log.i("-> FCT <-", "onTouchEvent up: dx = " + deltaX + " dy = " + deltaY);
                    if((abs(deltaX) > 5) || (abs(deltaY ) > 5)){
                        if(abs(deltaX) > abs(deltaY)){ // swipe horizantally
                            int i = (evtDownY - BottomRect.top) / carteTileSize;
                            if(deltaX < 0){ //to left
                                Log.i("-> FCT <-", "swip to left " + i);
                                int tmp = PlayerCard[i][0];
                                for(int j = 0; j < MatrixWidth - 1; j++)
                                    PlayerCard[i][j] = PlayerCard[i][j+1];
                                PlayerCard[i][MatrixWidth - 1] = tmp;
                            } else { // to right
                                Log.i("-> FCT <-", "swip to right " + i);
                                int tmp = PlayerCard[i][MatrixWidth - 1];
                                for(int j = MatrixWidth - 1; j > 0 ; j--)
                                    PlayerCard[i][j] = PlayerCard[i][j-1];
                                PlayerCard[i][0] = tmp;
                            }
                        }else if (abs(deltaX) < abs(deltaY)){ //swipe vertically
                            int j = (evtDownX - BottomRect.left) / carteTileSize;
                            if(deltaY < 0){ // to top
                                Log.i("-> FCT <-", "swip to top " + j);
                                int tmp = PlayerCard[0][j];
                                for(int i = 0; i < MatrixHeight - 1; i++)
                                    PlayerCard[i][j] = PlayerCard[i+1][j];
                                PlayerCard[MatrixHeight - 1][j] = tmp;
                            }else { // to bottom
                                Log.i("-> FCT <-", "swip to bottom " + j);
                                int tmp = PlayerCard[MatrixHeight - 1][j];
                                for(int i = MatrixHeight - 1; i > 0; i--)
                                    PlayerCard[i][j] = PlayerCard[i-1][j];
                                PlayerCard[0][j] = tmp;
                            }
                        }
                    }
                    isWon = isWon();
                    return true;
                default:
                    return super.onTouchEvent(event);
            }
        }
      return super.onTouchEvent(event);
    }

}