package org.eclipse.slm.common.keycloak.config;

/**
 * The type Security grants.
 *
 * @author des
 */
public class SecurityGrants {

    /**
     * The constant ROLE_PREFIX.
     */
    public static final String ROLE_PREFIX = "ROLE_";

    /**
     * The constant USER.
     */
    public static final String USER = "ssp-user";
    /**
     * The constant MODERATOR.
     */
    public static final String MODERATOR = "MODERATOR";
    /**
     * The constant ADMIN.
     */
    public static final String ADMIN = "ADMIN";
    /**
     * The constant ACTUATOR.
     */
    public static final String ACTUATOR = "ACTUATOR";

    /**
     * The constant ROLE_USER.
     */
    public static final String ROLE_USER = ROLE_PREFIX+USER;
    /**
     * The constant ROLE_MODERATOR.
     */
    public static final String ROLE_MODERATOR = ROLE_PREFIX+MODERATOR;
    /**
     * The constant ROLE_ADMIN.
     */
    public static final String ROLE_ADMIN = ROLE_PREFIX+ADMIN;
    /**
     * The constant ROLE_ACTUATOR.
     */
    public static final String ROLE_ACTUATOR = ROLE_PREFIX+ACTUATOR;

    /**
     * Get provided roles string [ ].
     *
     * @return the string [ ]
     */
    public static String[] getProvidedRoles(){
        return new String[]{
                USER,
                MODERATOR,
                ADMIN,
                ACTUATOR
        };
    }

    /**
     * Find by type security grants . role.
     *
     * @param type the type
     * @return the security grants . role
     */
    public static ROLE findByType(String type) {
        for(ROLE role: ROLE.values()) {
            if(role.name().equals(type)) {
                return role;
            }
        }
        return null;
    }

    /**
     * The enum Role.
     */
    public enum ROLE {

        /**
         * Role user role.
         */
        ROLE_USER(SecurityGrants.USER),
        /**
         * Role moderator role.
         */
        ROLE_MODERATOR(SecurityGrants.MODERATOR),
        /**
         * Role admin role.
         */
        ROLE_ADMIN(SecurityGrants.ADMIN),
        /**
         * Role actuator role.
         */
        ROLE_ACTUATOR(SecurityGrants.ACTUATOR);

        private final String value;

        ROLE(String value){
            this.value = value;
        }

        /**
         * Value string.
         *
         * @return the string
         */
        public final String value(){
            return this.value;
        }
    }

}
