package cxzgwing;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import cxzgwing.utils.FileUtils;
import cxzgwing.utils.NumberUtil;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class Window extends JFrame {
    // private final Logger logger = LoggerFactory.getLogger(Window.class);

    private static final String FIELD_PAUSE = "pause";
    private static final String FIELD_PLAY = "play";

    private static final int PROGRESS_HEIGHT = 10;
    private static final int PROGRESS_MIN_VALUE = 0;
    private static final int PROGRESS_MAX_VALUE = 100;
    private static final int WINDOW_X = 100;
    private static final int WINDOW_Y = 100;
    private static final int WINDOW_WIDTH = 850;
    private static final int WINDOW_HEIGHT = 600;
    private static final int LIST_WINDOW_WIDTH = 200;
    // 总时间
    private static String TOTAL_TIME;
    // 播放速度
    private float speed;
    // 首次播放
    private boolean firstPlay = true;

    // 播放器组件
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    // 进度条
    private JProgressBar progress;
    // 暂停按钮
    private Button pauseButton;
    // 显示播放速度的标签
    private Label displaySpeed;
    // 显示时间
    private Label displayTime;
    // 进度定时器
    private Timer progressTimer;
    // 继续播放定时器
    private Timer continueTimer;
    // 所有视频路径
    private java.util.List<String> videos;
    // 当前播放视频的位置
    private int videoIndex;
    // 声音控制进度条
    private JProgressBar volumeProgress;
    // 音量显示标签
    private Label volumeLabel;
    // 文件对话框
    private FileDialog fileDialog;
    // 播放文件列表按钮
    private Button listButton;
    // 播放文件列表窗口
    private JFrame listWindow;
    // 播放文件列表显示内容
    private JTextArea listContent;
    // 播放下一个视频按钮
    private Button nextButton;
    // 播放上一个视频按钮
    private Button previousButton;

    public Window() {
        this.videos = new ArrayList<>(10);

        // 设置默认速度为原速
        speed = 1.0f;
        // 设置窗口标题
        setTitle("VideoPlayer");
        // 设置窗口焦点监听事件：窗口打开时、窗口获得焦点时设置默认焦点为暂停按钮
        this.addWindowFocusListener(getWindowFocusListener());

        // 窗口关闭事件：释放资源并退出程序
        addWindowListener(closeWindowReleaseMedia());
        // 设置默认窗口关闭事件
        // setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 设置窗口位置
        setBounds(WINDOW_X, WINDOW_Y, WINDOW_WIDTH, WINDOW_HEIGHT);
        // 最大化显示窗口
        // setExtendedState(JFrame.MAXIMIZED_BOTH);

        // 主面板
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        // ======播放面板======
        JPanel player = new JPanel();
        contentPane.add(player, BorderLayout.CENTER);
        contentPane.add(player);
        player.setLayout(new BorderLayout(0, 0));
        // 创建播放器组件并添加到容器中去
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        player.add(mediaPlayerComponent);
        // 视频表面焦点监听：表面获得焦点时设置默认焦点为暂停按钮
        getVideoSurface().addFocusListener(videoSurfaceFocusAction());
        // getMediaPlayer().setRepeat(true); // 重复播放

        // ======底部面板======
        JPanel bottomPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(bottomPanel, BoxLayout.Y_AXIS);
        bottomPanel.setLayout(boxLayout);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        // ------进度条组件面板------
        JPanel progressPanel = new JPanel();
        progress = new JProgressBar();
        progress.setMinimum(PROGRESS_MIN_VALUE);
        progress.setMaximum(PROGRESS_MAX_VALUE);
        progress.setPreferredSize(getNewDimension());
        // 设置进度条中间显示进度百分比
        progress.setStringPainted(false);
        // 进度条进度的颜色
        progress.setForeground(new Color(46, 145, 228));
        // 进度条背景的颜色
        progress.setBackground(new Color(220, 220, 220));

        // 点击进度条调整视频播放指针
        progress.addMouseListener(setVideoPlayPoint());
        // 定时器
        progressTimer = getProgressTimer();

        progressPanel.add(progress);
        progressPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(progressPanel);
        // contentPane.add(progressPanel, BorderLayout.SOUTH);

        // ------按钮组件面板------
        JPanel buttonPanel = new JPanel();
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(buttonPanel);

        // 时间显示标签
        displayTime = new Label();
        displayTime.setText(getTimeString());
        buttonPanel.add(displayTime);

        // 选择文件按钮
        Button chooseButton = new Button("choose");
        fileDialog = new FileDialog(this);
        fileDialog.setMultipleMode(true);
        chooseButton.setFocusable(false);
        chooseButton.addMouseListener(mouseClickedChooseFiles());
        buttonPanel.add(chooseButton);

        // 上一个视频
        previousButton = new Button("previous");
        previousButton.setFocusable(false);
        previousButton.setEnabled(false);
        previousButton.addMouseListener(mouseClickedPlayPreviousVideo());
        buttonPanel.add(previousButton);

        // 后退按钮，每次后退5秒
        Button backButton = new Button("-5s");
        backButton.setFocusable(false);
        backButton.addMouseListener(mouseClickedBackVideo());
        buttonPanel.add(backButton);

        // 暂停/播放按钮
        pauseButton = new Button(FIELD_PLAY);
        pauseButton.setPreferredSize(new Dimension(49, 23));
        pauseButton.addKeyListener(spaceKeyPressMediaPause());
        pauseButton.addMouseListener(mouseClickedMediaPause());
        buttonPanel.add(pauseButton);

        // 前进按钮，每次前进5秒
        Button forwardButton = new Button("+5s");
        forwardButton.setFocusable(false);
        forwardButton.addMouseListener(mouseClickedForwardVideo());
        buttonPanel.add(forwardButton);

        // 下一个视频
        nextButton = new Button("next");
        nextButton.setFocusable(false);
        nextButton.setEnabled(false);
        nextButton.addMouseListener(mouseClickedPlayNextVideo());
        buttonPanel.add(nextButton);

        // 慢速播放按钮：每次递减0.1，最小为0.5倍速
        Button slowSpeedButton = new Button("<<<");
        slowSpeedButton.setFocusable(false);
        slowSpeedButton.addMouseListener(mouseClickedReducePlaySpeed());
        buttonPanel.add(slowSpeedButton);

        // 重置按钮：设置播放速度为原速
        Button resetButton = new Button("reset");
        resetButton.setFocusable(false);
        resetButton.addMouseListener(mouseClickedResetPlaySpeed());
        buttonPanel.add(resetButton);

        // 倍速播放按钮：每次递增0.1，最大为3倍速
        Button fastSpeedButton = new Button(">>>");
        fastSpeedButton.setFocusable(false);
        fastSpeedButton.addMouseListener(mouseClickedIncreasePlaySpeed());
        buttonPanel.add(fastSpeedButton);

        // 播放速度显示按钮
        displaySpeed = new Label();
        displaySpeed.setText("x" + speed);
        displaySpeed.setFocusable(false);
        displaySpeed.setEnabled(false);
        buttonPanel.add(displaySpeed);

        // 添加声音控制进度条
        volumeProgress = new JProgressBar();
        volumeProgress.setFocusable(false);
        volumeProgress.setMinimum(0);
        volumeProgress.setMaximum(100);
        volumeProgress.setValue(100);
        volumeProgress.setPreferredSize(new Dimension(100, 10));
        volumeProgress.addMouseListener(mouseClickedSetVolumeValue());
        buttonPanel.add(volumeProgress);

        // 音量显示
        volumeLabel = new Label();
        volumeLabel.setFocusable(false);
        volumeLabel.setEnabled(false);
        setVolumeLabel(volumeProgress.getValue());
        buttonPanel.add(volumeLabel);

        // 播放文件列表显示内容
        listContent = new JTextArea();
        listContent.setLineWrap(true);
        listContent.setFocusable(false);

        // 播放文件列表按钮
        listButton = new Button("list");
        listButton.setFocusable(false);
        listButton.addMouseListener(mouseClickedSetListWindow());
        buttonPanel.add(listButton);

        // 监听窗口大小，设置进度条宽度为窗口宽度（但是对于最大化和还原窗口无效，原因未知<-_->）
        this.addComponentListener(windowResizedResetProgressWidth());
        // 监听窗口最大化和还原，设置进度条宽度为窗口宽度
        this.addWindowStateListener(windowStateChangedResetProgressWidth());
        // 监听鼠标滑轮滚动，设置音量
        this.addMouseWheelListener(mouseWheelMovedSetVolume());
        this.addComponentListener(windowMovedAction());

        continueTimer = getContinueTimer();

        // 设置窗口最小值
        this.setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        // 设置窗口可见
        this.setVisible(true);
    }

    /**
     * 鼠标点击播放下一个视频
     */
    private MouseAdapter mouseClickedPlayNextVideo() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (videos.isEmpty() || videoIndex < 0 || videoIndex == videos.size() - 1) {
                    return;
                }
                reInitPlay(1);
            }
        };
    }

    /**
     * 重新初始化视频
     * 
     * @param offset 偏移量，1为下一个视频，-1为上一个视频
     */
    private void reInitPlay(int offset) {
        videoIndex += offset;
        if (videoIndex < 0 || videoIndex >= videos.size()) {
            return;
        }
        EmbeddedMediaPlayer mediaPlayer = getMediaPlayer();
        if (!Objects.isNull(mediaPlayer)) {
            mediaPlayer.stop();
        }
        firstPlay = true;
        progressTimer.stop();
        continueTimer.stop();
        initPlay();
    }

    /**
     * 鼠标点击播放上一个视频
     */
    private MouseAdapter mouseClickedPlayPreviousVideo() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (videos.isEmpty() || videoIndex <= 0) {
                    return;
                }
                reInitPlay(-1);
            }
        };
    }

    /**
     * 鼠标点击回退视频，往前退5秒
     */
    private MouseAdapter mouseClickedBackVideo() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                adaptVideoTime(-5000);
            }
        };
    }

    /**
     * 调整视频时间（前进或回退具体多少毫秒时间）
     * 
     * @param offset 偏移量，毫秒
     */
    private void adaptVideoTime(int offset) {
        if (firstPlay) {
            return;
        }
        getMediaPlayer().setTime(getMediaPlayer().getTime() + offset);
    }

    /**
     * 鼠标点击前进视频，前进5秒
     */
    private MouseAdapter mouseClickedForwardVideo() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                adaptVideoTime(5000);
            }
        };
    }

    /**
     * 主窗口移动监听，窗口移动则隐藏播放列表
     */
    private ComponentAdapter windowMovedAction() {
        return new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                setListWindowInvisible();
            }
        };
    }

    /**
     * 鼠标点击设置播放列表窗口
     */
    private MouseAdapter mouseClickedSetListWindow() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listWindow == null) {
                    // 播放文件列表窗口
                    listWindow = new JFrame();
                    listWindow.add(listContent);
                    listWindow.setUndecorated(true);
                    // 设置透明度
                    listWindow.setOpacity(0.8f);
                    setListWindowBounds();
                    listWindow.setVisible(true);
                    setListButtonColorWhenListWindowShown();
                    listWindow.addComponentListener(
                            setListButtonBackgroundWhenListWindowShownOrHidden());
                    return;
                }
                int x = getX();
                int width = getWidth();
                if (WINDOW_X != x || WINDOW_WIDTH != width) {
                    setListWindowBounds();
                }
                boolean visible = listWindow.isVisible();
                if (visible) {
                    listWindow.setVisible(false);
                } else {
                    listWindow.setVisible(true);
                }
            }
        };
    }

    /**
     * 当播放列表隐藏或显示时，设置播放列表按钮的背景颜色
     */
    private ComponentAdapter setListButtonBackgroundWhenListWindowShownOrHidden() {
        return new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                setListButtonColorWhenListWindowShown();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                setListButtonColorWhenListWindowHidden();
            }
        };
    }

    /**
     * 当播放列表隐藏时，设置播放列表按钮背景颜色
     */
    private void setListButtonColorWhenListWindowHidden() {
        listButton.setBackground(new Color(238, 238, 238));
    }

    /**
     * 当播放列表显示时，设置播放列表按钮背景颜色
     */
    private void setListButtonColorWhenListWindowShown() {
        listButton.setBackground(new Color(141, 141, 141));
    }

    /**
     * 设置播放列表边界
     */
    private void setListWindowBounds() {
        if (listWindow != null) {
            listWindow.setBounds(getWidth() + getX() - LIST_WINDOW_WIDTH - 6, getY() + 37,
                    LIST_WINDOW_WIDTH - 8, getHeight() - 100);
        }
    }

    /**
     * 鼠标点击选择文件
     */
    private MouseAdapter mouseClickedChooseFiles() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fileDialog.setVisible(true);
                File[] files = fileDialog.getFiles();
                videos.clear();
                listContent.setText("");
                for (File file : files) {
                    videos.add(file.getAbsolutePath());
                    listContent.append(videos.size() + "." + file.getName() + "\n");
                }
                videos.sort(Comparator.naturalOrder());
                if (videos.size() <= 1) {
                    previousButton.setEnabled(false);
                    nextButton.setEnabled(false);
                } else {
                    previousButton.setEnabled(true);
                    nextButton.setEnabled(true);
                }
                if (!Objects.isNull(getMediaPlayer())) {
                    getMediaPlayer().stop();
                }
                pauseButton.setLabel(FIELD_PLAY);
                firstPlay = true;
                setProgress(0, 0);
                progressTimer.stop();
                continueTimer.stop();
                videoIndex = 0;
                preLoading();
            }
        };
    }

    /**
     * 鼠标轮滑滚动设置视频播放音量，最小为0%，最大为100%
     */
    private MouseAdapter mouseWheelMovedSetVolume() {
        return new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // 1-下，-1-上
                int wheelRotation = e.getWheelRotation();
                if (wheelRotation == 1) {
                    // 减小音量
                    setVolume(volumeProgress.getValue() - 5);
                } else if (wheelRotation == -1) {
                    // 增大音量
                    setVolume(volumeProgress.getValue() + 5);
                }
            }
        };
    }

    /**
     * 设置当前视频播放音量标签显示的百分比
     * 
     * @param value 当前播放音量，0-100
     */
    private void setVolumeLabel(int value) {
        volumeLabel.setText(value + "%");
    }

    /**
     * 鼠标点击设置视频播放音量
     */
    private MouseAdapter mouseClickedSetVolumeValue() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVolume(e.getX());
            }
        };
    }

    /**
     * 设置视频播放音量
     * 
     * @param value 音量，0-100
     */
    private void setVolume(int value) {
        if (value < 0) {
            value = 0;
        } else if (value > 100) {
            value = 100;
        }
        if (volumeProgress.getValue() == value) {
            return;
        }
        volumeProgress.setValue(value);
        setVolumeLabel(value);
        getMediaPlayer().setVolume(value);
    }

    /**
     * 预加载视频
     */
    private void preLoading() {
        if (videos.isEmpty()) {
            return;
        }
        String path = videos.get(videoIndex);
        setTitle("VideoPlayer-" + FileUtils.getFileName(path) + "（预加载）");
    }

    /**
     * 初始化视频
     */
    private void initPlay() {
        if (videos.isEmpty()) {
            return;
        }
        getMediaPlayer().playMedia(videos.get(videoIndex));
        setWindowTitle();
        String label = pauseButton.getLabel();
        if (!FIELD_PAUSE.equals(label)) {
            pauseButton.setLabel(FIELD_PAUSE);
        }
        setProgress(getMediaPlayer().getTime(), getMediaPlayer().getLength());
        progressTimer.start();
        continueTimer.start();
        this.firstPlay = false;
    }

    /**
     * 设置主窗口标题：VideoPlayer-视频名称
     */
    private void setWindowTitle() {
        String title = getMediaPlayer().getMediaMeta().getTitle();
        setTitle("VideoPlayer-" + title);
    }

    /**
     * 获取播放时间显示字符串
     * 
     * @param curr 当前时间
     * @param total 总时间
     * @return 播放时间显示字符串，格式00:00:00/00:00:00
     */
    private String getTimeString(long curr, long total) {
        return formatSecond2Time(curr) + " / " + formatSecond2Time(total);
    }

    /**
     * 获取播放时间显示字符串
     * 
     * @return 播放时间显示字符串，格式00:00:00/00:00:00
     */
    private String getTimeString() {
        setTotalTime();
        return formatSecond2Time(getMediaPlayer().getTime()) + " / " + TOTAL_TIME;
    }

    /**
     * 设置播放时间显示字符串中的总时间
     */
    private void setTotalTime() {
        if (TOTAL_TIME == null) {
            long totalSecond = getMediaPlayer().getLength();
            TOTAL_TIME = formatSecond2Time(totalSecond);
        }
    }

    /**
     * 格式化时间，将秒转化为时分秒
     * 
     * @param milliseconds 毫秒时间
     * @return 00:00:00
     */
    private String formatSecond2Time(long milliseconds) {
        int second = (int) (milliseconds / 1000);
        int h = second / 3600;
        int m = (second % 3600) / 60;
        int s = (second % 3600) % 60;
        return String.format("%02d", h) + ":" + String.format("%02d", m) + ":"
                + String.format("%02d", s);
    }

    /**
     * 视频继续播放定时器
     */
    private Timer getContinueTimer() {
        return new Timer(1000, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long total = getMediaPlayer().getLength();
                long curr = getMediaPlayer().getTime();
                if (curr == total) {
                    videoIndex++;
                    if (videoIndex >= videos.size()) {
                        continueTimer.stop();
                        System.out.println("all videos finished...");
                        return;
                    }
                    getMediaPlayer().playMedia(videos.get(videoIndex));
                    setWindowTitle();
                    setProgress(getMediaPlayer().getTime(), getMediaPlayer().getLength());
                    progressTimer.restart();
                }
            }
        });
    }

    /**
     * 视频播放进度条定时器
     */
    private Timer getProgressTimer() {
        return new Timer(1000, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getProgress().getValue() >= PROGRESS_MAX_VALUE) {
                    // 结束定时器
                    progressTimer.stop();
                    return;
                }
                // 设置进度值
                setProgress(getMediaPlayer().getTime(), getMediaPlayer().getLength());
            }
        });
    }

    /**
     * 设置进度条
     * 
     * @param curr 当前时间，毫秒
     * @param total 总时间，毫秒
     */
    private void setProgress(long curr, long total) {
        float percent = (float) curr / total;
        int value = (int) (percent * 100);
        getProgress().setValue(value);
        displayTime.setText(getTimeString(curr, total));
    }

    /**
     * 主窗口状态监听器，当窗口状态改变时（窗口恢复到初始状态或窗口最大化），重新绘制进度条
     */
    private WindowAdapter windowStateChangedResetProgressWidth() {
        return new WindowAdapter() {
            @Override
            public void windowStateChanged(WindowEvent state) {
                // state=1或7为最小化，此处不处理

                if (state.getNewState() == 0) {
                    // System.out.println("窗口恢复到初始状态");
                    setProgressWidthAutoAdaptWindow();
                    setListWindowInvisible();
                    setListWindowBounds();
                } else if (state.getNewState() == 6) {
                    // System.out.println("窗口最大化");
                    setProgressWidthAutoAdaptWindow();
                    setListWindowInvisible();
                    setListWindowBounds();
                }
            }
        };
    }

    /**
     * 设置播放列表隐藏
     */
    private void setListWindowInvisible() {
        if (listWindow != null && listWindow.isVisible()) {
            listWindow.setVisible(false);
        }
    }

    /**
     * 设置进度条自适应主窗口
     */
    private void setProgressWidthAutoAdaptWindow() {
        getProgress().setPreferredSize(getNewDimension());
    }

    /**
     * 主窗口大小变化监听器，当主窗口大小改变时，重绘进度条，并隐藏播放视频列表窗口
     */
    private ComponentAdapter windowResizedResetProgressWidth() {
        return new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                setProgressWidthAutoAdaptWindow();
                setListWindowInvisible();
            }
        };
    }

    /**
     * 获取形状
     */
    private Dimension getNewDimension() {
        return new Dimension(getWidth(), PROGRESS_HEIGHT);
    }

    /**
     * 设置视频播放指针
     */
    private MouseAdapter setVideoPlayPoint() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                long total = getMediaPlayer().getLength();
                long time = (long) ((float) x / progress.getWidth() * total);
                setProgress(time, total);
                getMediaPlayer().setTime(time);
            }
        };
    }

    /**
     * 视频表面焦点监听器，当视频表面获取焦点时，设置暂停按钮为主窗口默认焦点
     */
    private FocusAdapter videoSurfaceFocusAction() {
        return new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                setPauseButtonAsDefaultFocus();
            }
        };
    }

    /**
     * 窗口关闭监听器，当窗口关闭时，暂停视频、释放视频资源、退出程序
     */
    private WindowAdapter closeWindowReleaseMedia() {
        return new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                getMediaPlayer().stop();
                getMediaPlayer().release();
                System.exit(0);
            }
        };
    }

    /**
     * 鼠标点击减缓播放速度，减0.1
     */
    private MouseListener mouseClickedReducePlaySpeed() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (speed <= 0.5f) {
                    speed = 1.0f;
                } else {
                    speed -= 0.1f;
                }
                speed = (float) NumberUtil.formatNumber(speed, 1);
                getMediaPlayer().setRate(speed);
                displaySpeed.setText("x" + speed);
            }
        };
    }

    /**
     * 鼠标点击设置播放速度，原速，1.0倍
     */
    private MouseListener mouseClickedResetPlaySpeed() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (speed == 1.0f) {
                    return;
                }
                speed = 1.0f;
                getMediaPlayer().setRate(speed);
                displaySpeed.setText("x" + speed);
            }
        };
    }

    /**
     * 鼠标点击增加播放速度，加0.1
     */
    private MouseListener mouseClickedIncreasePlaySpeed() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (speed >= 3.0f) {
                    speed = 1.0f;
                } else {
                    speed += 0.1f;
                }
                speed = (float) NumberUtil.formatNumber(speed, 1);
                getMediaPlayer().setRate(speed);
                displaySpeed.setText("x" + speed);
            }
        };
    }

    /**
     * 鼠标点击暂停视频
     */
    private MouseAdapter mouseClickedMediaPause() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (videos.isEmpty()) {
                    return;
                }
                if (firstPlay) {
                    initPlay();
                    return;
                }
                setMediaStatusAndPauseButton();
                if (progressTimer.isRunning()) {
                    progressTimer.stop();
                } else {
                    progressTimer.restart();
                }
            }
        };
    }

    /**
     * 设置视频状态（播放或暂停），同时设置播放暂停按钮文案
     */
    private void setMediaStatusAndPauseButton() {
        if (getMediaPlayer().isPlaying()) {
            getMediaPlayer().pause();
            pauseButton.setLabel(FIELD_PLAY);
        } else {
            getMediaPlayer().play();
            pauseButton.setLabel(FIELD_PAUSE);
        }
    }

    /**
     * 获取主窗口焦点监听器，设置默认焦点为播放暂停按钮
     */
    private WindowFocusListener getWindowFocusListener() {
        return new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                setPauseButtonAsDefaultFocus();
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {
                setPauseButtonAsDefaultFocus();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {}
        };
    }

    /**
     * 设置默认焦点为播放暂停按钮
     */
    private void setPauseButtonAsDefaultFocus() {
        pauseButton.requestFocus();
    }

    /**
     * 键盘空格监听器，按下空格后，暂停或播放视频，同时修改播放暂停按钮文案
     */
    private KeyListener spaceKeyPressMediaPause() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (videos.isEmpty()) {
                    return;
                }
                if (firstPlay) {
                    initPlay();
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    setMediaStatusAndPauseButton();
                }
            }
        };
    }

    /**
     * 获取进度条实例
     */
    private JProgressBar getProgress() {
        return progress;
    }

    /**
     * 获取视频实例
     */
    private EmbeddedMediaPlayer getMediaPlayer() {
        return mediaPlayerComponent.getMediaPlayer();
    }

    /**
     * 获取视频表面
     */
    private Canvas getVideoSurface() {
        return mediaPlayerComponent.getVideoSurface();
    }

}
