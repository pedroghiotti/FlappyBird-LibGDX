package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class MyGdxGame extends ApplicationAdapter
{
	private SpriteBatch batch; // Renderiza as texturas e textos
	private ShapeRenderer shapeRenderer; // Permite criar formas. Usado pra definir colisores dos objetos
	private OrthographicCamera camera; // Camera renderiza a tela do jogo
	private Viewport viewport; // Gerencia dimensões da tela
	private Random random; // Classe permite gerar números aleatórios
	private Preferences preferences; // Classe usada para guardar high score na memória do dispositivo

	// Variáveis guardam referência às texturas
	private Texture[] texArray_playerAnimFrames;
	private Texture tex_background;
	private Texture tex_pipeTop;
	private Texture tex_pipeBottom;
	private Texture tex_gameOver;
	private Texture tex_coinSilver;
	private Texture tex_coinGold;
	private Texture tex_coinCurrent;
	private Texture tex_title;
	
	// Variáveis guardam referência aos assets de som
	private Sound soundWingFlap;
	private Sound soundCollision;
	private Sound soundScore;
	private Sound soundCoin;

	// Variáveis guardam valores usados pra configuração do jogo.
	private float scrollSpeedBase = 400;
	private float scrollSpeedMax = 1000;
	private float scrollSpeedIncrement = 10;
	private float pipesGapSize = 350;
	private float playerPosHorizontalOffset = 50;
	private float playerSpriteScale = .25f;
	private float titleSpriteScale = .25f;
	private float coinValueSilver = 5;
	private float coinValueGold = 10;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;

	// Shapes usados como colisor para os elementos do jogo
	private Rectangle collider_pipeTop;
	private Rectangle collider_pipeBottom;
	private Circle collider_player;
	private Circle collider_coin;
	
	// Textos desenhados na tela
	private BitmapFont txt_score;
	private BitmapFont txt_reset;
	private BitmapFont txt_highScore;
	
	// Variáveis guardam valores relativos às posições e outros elementos para funcionamento do jogo (tipo da moeda sendo desenhada, dimensões do dispositivo, estado do jogo, pontuação)
	private float playerAnimFrame = 0;
	private float coinType = 0;
	private float scrollSpeedCurrent;
	private float playerPosY = 0;
	private float playerPosX;
	private float playerPosDownwardOffset = 2;
	private float pipePosX;
	private float pipePosY;
	private float coinPosX;
	private float coinPosXOffset;
	private float coinPosY;
	private float deviceWidth;
	private float deviceHeight;
	private int gameState = 0;
	private int score = 0;
	private int highScore = 0;
	private boolean hasPassedPipes = false;

	/*
		Método roda quando aplicação é aberta.
		Pega referências aos assets,
		inicializa valores das variáveis.
	*/
	@Override
	public void create ()
	{
		initializeAssets();
		initializeObjects();
	}

	/*
		Método roda a cada frame.
		Roda a lógica do jogo,
		desenha tela do jogo.
	*/
	@Override
	public void render ()
	{
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		checkGameState();
		validateScore();
		drawObjects();
		drawUI();
		detectCollisions();
		animatePlayerSprite();
	}

	/*
		Pega referências aos assets.
	*/
	private void initializeAssets()
	{
		texArray_playerAnimFrames = new Texture[3];

		texArray_playerAnimFrames[0] = new Texture("passaro1.png");
		texArray_playerAnimFrames[1] = new Texture("passaro2.png");
		texArray_playerAnimFrames[2] = new Texture("passaro3.png");

		tex_background = new Texture("fundo.png");
		tex_pipeBottom = new Texture("cano_baixo_maior.png");
		tex_pipeTop = new Texture("cano_topo_maior.png");
		tex_gameOver = new Texture("game_over.png");
		tex_coinGold = new Texture("moeda_ouro.png");
		tex_coinSilver = new Texture("moeda_prata.png");
		tex_title = new Texture("logo.png");

		soundWingFlap = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		soundCollision = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		soundScore = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));
		soundCoin = Gdx.audio.newSound(Gdx.files.internal("som_moeda.mp3"));
	}

	/*
		Inicializa valores das variáveis.
	*/
	private void initializeObjects()
	{
		batch = new SpriteBatch();
		random = new Random();
		shapeRenderer = new ShapeRenderer();

		tex_coinCurrent = tex_coinSilver;

		deviceWidth = VIRTUAL_WIDTH;
		deviceHeight = VIRTUAL_HEIGHT;

		playerPosX = playerPosHorizontalOffset;
		playerPosY = deviceHeight / 2;
		pipePosX = deviceWidth;
		coinPosX = pipePosX + deviceWidth / 2;

		txt_score = new BitmapFont();
		txt_score.setColor(Color.WHITE);
		txt_score.getData().setScale(10);
		txt_reset = new BitmapFont();
		txt_reset.setColor(Color.GREEN);
		txt_reset.getData().setScale(2);
		txt_highScore = new BitmapFont();
		txt_highScore.setColor(Color.RED);
		txt_highScore.getData().setScale(2);

		collider_player = new Circle();
		collider_pipeBottom = new Rectangle();
		collider_pipeTop = new Rectangle();
		collider_coin = new Circle();

		preferences = Gdx.app.getPreferences("flappyBird");
		highScore = preferences.getInteger("pontuacaoMaxima", 0);

		scrollSpeedCurrent = scrollSpeedBase;

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
	}

	/*
		Recebe Input.
		Define funcionamento do jogo com base no gameState.

		gameState == 0 -> Esperando input do jogador pra iniciar jogo.
		gameState == 1 -> Jogo rodando.
		gameState == 2 -> Tela Game Over.
	*/
	private void checkGameState()
	{
		boolean toqueTela = Gdx.input.justTouched();

		if(gameState == 0)
		{
			if (toqueTela == true)
			{
				playerFlapWings();
				gameState = 1;
			}
		}
		else if(gameState == 1)
		{
			moveSceneObjects();

			if(toqueTela == true) playerFlapWings();

			if(playerPosY > 0 || toqueTela)
			{
				playerPosY = playerPosY - playerPosDownwardOffset;
			}

			playerPosDownwardOffset++;
		}
		else if(gameState == 2)
		{
			playerPosX -= Gdx.graphics.getDeltaTime()*500;

			if(score > highScore) updateHighScore();
			if(toqueTela == true) resetGameState();
		}
	}

	/*
		Desenha objetos do jogo.
	*/
	private void drawObjects()
	{
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		batch.draw // Desenha fundo
		(
			tex_background,
			0,
			0,
			deviceWidth,
			deviceHeight
		);
		batch.draw // Desenha player
		(
			texArray_playerAnimFrames[(int) playerAnimFrame],
			playerPosX,
			playerPosY,
			texArray_playerAnimFrames[0].getWidth() * playerSpriteScale,
			texArray_playerAnimFrames[0].getHeight() * playerSpriteScale
		);
		batch.draw // Desenha cano de baixo
		(
			tex_pipeBottom,
			pipePosX,
			deviceHeight / 2 - tex_pipeBottom.getHeight() - pipesGapSize / 2 + pipePosY
		);
		batch.draw // Desenha cano do topo
		(
			tex_pipeTop,
			pipePosX,
			deviceHeight / 2 + pipesGapSize / 2 + pipePosY
		);
		batch.draw // Desenha moedas
		(
			tex_coinCurrent,
			coinPosX,
			coinPosY
		);

		batch.end();
	}

	/*
		Desenha UI;
	*/
	private void drawUI()
	{
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		if(gameState == 0)
		{
			batch.draw // Desenha Título do game.
			(
				tex_title,
				(deviceWidth / 2) - ((tex_title.getWidth() * titleSpriteScale) / 2),
				(deviceHeight - 100) - (tex_title.getHeight() * titleSpriteScale),
				tex_title.getWidth() * titleSpriteScale,
				tex_title.getHeight() * titleSpriteScale
			);
		}

		if(gameState == 1 || gameState == 2)
		{
			txt_score.draw // Desenha texto pontuação atual.
			(
				batch,
				String.valueOf(score),
				deviceWidth / 2 - 50,
				deviceHeight - 110
			);
		}

		if(gameState == 2)
		{
			batch.draw // Desenha textura de GameOver.
			(
				tex_gameOver,
				deviceWidth /2 - tex_gameOver.getWidth()/2,
				deviceHeight /2
			);
			txt_reset.draw // Desenha texto reiniciar.
			(
				batch,
				"Toque para reiniciar!",
				deviceWidth /2 - 140,
				deviceHeight /2 - tex_gameOver.getHeight()/2
			);
			txt_highScore.draw // Desenha texto pontuação mais alta.
			(
				batch,
				"Seu record é: " + highScore + " pontos!",
				deviceWidth /2 - 165,
				deviceHeight /2 - tex_gameOver.getHeight()
			);
		}

		batch.end();
	}

	/*
		Posiciona colisores,
		detecta colisões,
		executa código para colisões.
	*/
	private void detectCollisions()
	{
		/*
			Posiciona colisores dos objetos na tela.
		*/
		collider_player.set // Posiciona colisor jogador.
		(
				playerPosX + (texArray_playerAnimFrames[0].getWidth() * playerSpriteScale) / 2,
				playerPosY + (texArray_playerAnimFrames[0].getHeight() * playerSpriteScale) / 2,
				(texArray_playerAnimFrames[0].getWidth() * playerSpriteScale) / 2
		);
		collider_pipeBottom.set // Posiciona colisor cano de baixo.
		(
			pipePosX,
			deviceHeight / 2 - tex_pipeBottom.getHeight() - pipesGapSize /2 + pipePosY,
			tex_pipeBottom.getWidth(),
			tex_pipeBottom.getHeight()
		);
		collider_pipeTop.set // Posiciona colisor cano de cima.
		(
			pipePosX,
			deviceHeight / 2 + pipesGapSize / 2 + pipePosY,
			tex_pipeTop.getWidth(),
			tex_pipeTop.getHeight()
		);
		collider_coin.set // Posiciona colisor moeda.
		(
				coinPosX + tex_coinCurrent.getWidth() / 2,
				coinPosY + tex_coinCurrent.getHeight() / 2,
			tex_coinCurrent.getHeight() / 2
		);

		/*
			Verifica se houve colisões.
			Executa reação apropriada à essas colisões.
		*/
		boolean hasCollidedPipeTop = Intersector.overlaps(collider_player, collider_pipeTop);
		boolean hasCollidedPipeBottom = Intersector.overlaps(collider_player, collider_pipeBottom);
		boolean hasCollidedCoin = Intersector.overlaps(collider_player, collider_coin);

		if(hasCollidedCoin)
		{
			coinPosY = deviceHeight * 2; // Posiciona moeda fora da tela para que não seja coletada várias vezes.

			// Adiciona pontuação dependendo do tipo da moeda coletada.
			if(coinType == 0) score += coinValueSilver;
			else if(coinType == 1) score += coinValueGold;

			soundCoin.play();
		}
		if(hasCollidedPipeTop == true || hasCollidedPipeBottom == true)
		{
			// Atualiza gameState -> gameOver.
			if(gameState == 1)
			{
				soundCollision.play();
				gameState = 2;
			}
		}
	}

	/*
		Adiciona à pontuação do jogador caso tenha passado entre os canos.
	*/
	private void validateScore()
	{
		if(pipePosX < playerPosX)
		{
			if(hasPassedPipes == false)
			{
				score++;
				hasPassedPipes = true;
				soundScore.play();

				if(scrollSpeedCurrent < scrollSpeedMax)
				{
					scrollSpeedCurrent += scrollSpeedIncrement;
				}
			}
		}
	}

	/*
		Faz animação do jogador.
	*/
	private void animatePlayerSprite()
	{
		playerAnimFrame += Gdx.graphics.getDeltaTime() * 10;
		if(playerAnimFrame > 3)
		{
			playerAnimFrame = 0;
		}
	}

	/*
		Movimenta os objetos da cena (moedas e canos).
	*/
	private void moveSceneObjects()
	{
		float timeElapsedSincelastFrame = Gdx.graphics.getDeltaTime();

		pipePosX -= timeElapsedSincelastFrame * scrollSpeedCurrent;
		coinPosX -= timeElapsedSincelastFrame * scrollSpeedCurrent;

		if(pipePosX < -tex_pipeTop.getWidth())
		{
			pipePosX = deviceWidth;
			pipePosY = random.nextInt(400) - 200;
			hasPassedPipes = false;
		}

		if(coinPosX < -tex_coinCurrent.getWidth())
		{
			coinPosXOffset = random.nextInt((int) deviceWidth - 2 * tex_pipeBottom.getWidth()) + tex_pipeBottom.getWidth();
			coinPosX = pipePosX + coinPosXOffset;
			coinPosY = random.nextInt((int) deviceHeight - tex_coinCurrent.getHeight());

			// Randomiza o tipo da moeda e atualiza a textura.
			coinType = random.nextInt(100) <= 75 ? 0 : 1;
			if(coinType == 0) tex_coinCurrent = tex_coinSilver;
			else if(coinType == 1) tex_coinCurrent = tex_coinGold;
		}
	}

	/*
		Faz a movimentação do player (joga posição pra cima).
	*/
	private void playerFlapWings()
	{
		playerPosDownwardOffset = -15;
		soundWingFlap.play();
	}

	/*
		Atualiza pontuação mais alta.
	*/
	private void updateHighScore()
	{
		highScore = score;
		preferences.putInteger("pontuacaoMaxima", highScore);
		preferences.flush();
	}

	/*
		Reseta estado do jogo.
	*/
	private void resetGameState()
	{
		gameState = 0;
		score = 0;

		playerPosX = playerPosHorizontalOffset;
		playerPosY = deviceHeight /2;
		playerPosDownwardOffset = 0;

		pipePosX = deviceWidth;

		coinPosX = pipePosX + deviceWidth / 2;

		scrollSpeedCurrent = scrollSpeedBase;
	}

	@Override
	public void resize(int width, int height) { viewport.update(width, height); }
	@Override
	public void dispose() {}
}
