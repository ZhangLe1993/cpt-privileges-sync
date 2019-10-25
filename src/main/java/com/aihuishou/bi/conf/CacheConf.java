package com.aihuishou.bi.conf;

public class CacheConf {

    /**
     * 菜单和权限的映射缓存 key，形如{"aaa":["xxx","yyy"],"bbb":["xxx","yyy"]}
     */
    public final static String MAP_MENU_AUTH = "map-menu-auth";

    /**
     * 菜单权限缓存 [{"position":"xxx","authName":"yyyy"},{"position":"xxx","authName":"yyyy"}]
     */
    public final static String LIST_NODE_AUTH = "list-node-auth";

    /**
     * 某一用户所拥有的所有权限集合["xxx","yyyy"]
     */
    public final static String LIST_USER_AUTH = "list-user-auth";

    /**
     * 某一用户所拥有的所有权限集合["xxx","yyyy"]
     */
    public final static String LIST_USER_AUTH_MONGO = "list-user-auth-mongo";

    /**
     * 菜单映射，比如传入的是shanghaiOperationXXX  指向的是  OperationXXX，结果为 {id:"", source : "", target : "", key : "", value : ""}
     */
    public final static String POSITION_MAPPING_MODEL = "position-mapping-model";

    /**
     * 所有菜单的
     */
    public final static String POSITION_MAPPING_MAP = "position-mapping-map";


    public final static String POSITION_MAP = "position-map";

    /**
     * 精致提炼菜单
     */
    public final static String REFINED_MENU = "refined-menu";

    /**
     * 当前登录用户
     */
    public final static String CURRENT_USER = "current-user";


}
