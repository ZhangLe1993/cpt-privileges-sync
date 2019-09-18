# 测试环境
    # 主进程启动
    java -jar cpt-privileges-sync-0.0.1-release.jar --spring.profiles.active=dev
    # 守护进程启动
    nohup java -jar cpt-privileges-sync-0.0.1-release.jar --spring.profiles.active=dev &
# 生产环境
    # 主进程启动
    java -jar cpt-privileges-sync-0.0.1-release.jar --spring.profiles.active=prod
    # 守护进程启动
    nohup java -jar cpt-privileges-sync-0.0.1-release.jar --spring.profiles.active=prod &

