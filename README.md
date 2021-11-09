# video-player

![image](https://img.shields.io/github/license/cxzgwing/simple-video-player2?style=flat-square)

Java简易视频播放器 升级版本

原版本见https://github.com/cxzgwing/simple-video-player

## 说明
vlc资源（zip压缩包）在https://github.com/cxzgwing/simple-video-player2-vlc

vlc目录包含plugins文件夹、libvlc.dll文件、libvlccore.dll文件

解压后放至simple-video-player2目录下，vlc目录与src目录平级

vlc官网：https://www.videolan.org/vlc/

**另**也可使用原版本中的vlc资源

## 效果图
![03](https://user-images.githubusercontent.com/41880446/121799729-4286f680-cc60-11eb-9217-faef317768ad.png)

## 项目与工具
maven(maven-archetype-quickstart)、java-1.8、vlc-3.8.0、log4j-2.13.3

maven-assembly-plugin打包工具（包含所有依赖，simple-video-player2-1.0-SNAPSHOT-jar-with-dependencies.jar需从target目录复制到项目根目录simple-video-player2下才可使用，目前的问题：jar包只能播放英文名的视频文件）

## 功能说明

1、播放时间/总时间显示

2、选择文件按钮（choose）：选择文件（可多选）确定后，点击play播放首个视频（排序规则为视频文件名字符串排序），可重复多次选择文件，若当前在播放视频，选择文件后自动停止播放，并预加载选择的首个视频

3、播放上一个视频按钮（previous）：选择单个视频时不激活，选择多个视频时激活，播放非第一个视频时有效

4、后退5秒按钮（-5s）：播放视频时点击可后退5秒

5、播放暂停按钮（play）：播放视频或暂停视频（按钮文案动态显示），绑定的快捷键为空格，当窗口获得焦点时，按空格可播放或暂停视频

6、前进5秒按钮（+5s）：播放视频时点击可前进5秒

7、播放下一个视频按钮（next）：选择单个视频时不激活，选择多个视频时激活，播放非最后一个视频时有效

8、慢速播放按钮（<<<）：播放速度减0.1，最小0.5倍速

9、重置播放速度按钮（reset）：重置播放速度为原速播放（1.0倍）

10、快速播放按钮（>>>）：播放速度加0.1，最大3.0倍速

11、播放速度显示

12、音量显示与控制：音量进度条可点击设置音量，当窗口获得焦点后滚动鼠标轮滑课设置音量（变量为10%）

13、播放列表显示按钮（list）：显示播放列表（可换行显示，半透明效果），当窗口移动时、窗口最大化时、窗口还原时自动隐藏播放列表，播放列表文件排序规则为视频文件名字符串排序（该顺序同时也是播放顺序）

14、视频播放进度条：可随窗口自适应宽度（当窗口调整大小时，疑似重绘进度条跟不上，会有进度条与窗口宽度不一致的情况，目前暂未解决）
