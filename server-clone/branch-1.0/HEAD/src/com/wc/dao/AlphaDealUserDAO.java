package com.wc.dao;
// default package

import com.wc.bean.AlphaDealUser;
import com.wc.jpa.EntityManagerHelper;

import java.util.logging.Level;

import javax.persistence.EntityManager;

/**
 	* A data access object (DAO) providing persistence and search support for WcUser entities.
 	 		* Transaction control of the save(), update() and delete() operations must be handled externally by senders of these methods 
 		  or must be manually added to each of these methods for data to be persisted to the JPA datastore.	
 	 * @see .WcUser
  * @author MyEclipse Persistence Tools 
 */
public class AlphaDealUserDAO  {
	//property constants
	public static final String USER_ID = "userId";
	public static final String RUNNING = "running";

	private EntityManager getEntityManager() {
		return EntityManagerHelper.getEntityManager();
	}	
	
		/**
	 Perform an initial save of a previously unsaved WcUser entity. 
	 All subsequent persist actions of this entity should use the #update() method.
	 This operation must be performed within the a database transaction context for the entity's data to be permanently saved to the persistence store, i.e., database. 
	 This method uses the {@link javax.persistence.EntityManager#persist(Object) EntityManager#persist} operation.
	 	 
	 * <pre> 
	 *   EntityManagerHelper.beginTransaction();
	 *   WcUserDAO.save(entity);
	 *   EntityManagerHelper.commit();
	 * </pre>
	   @param entity WcUser entity to persist
	  @throws RuntimeException when the operation fails
	 */
    public void save(AlphaDealUser entity) {
    				EntityManagerHelper.log("saving Alphadeal instance", Level.INFO, null);
	        try {
	        	getEntityManager().getTransaction().begin();
            getEntityManager().persist(entity);
            getEntityManager().getTransaction().commit();
            			EntityManagerHelper.log("save successful", Level.INFO, null);
	        } catch (RuntimeException re) {
        				EntityManagerHelper.log("save failed", Level.SEVERE, re);
	            throw re;
        }
    }
    
    /**
	 Delete a persistent WcUser entity.
	  This operation must be performed 
	 within the a database transaction context for the entity's data to be
	 permanently deleted from the persistence store, i.e., database. 
	 This method uses the {@link javax.persistence.EntityManager#remove(Object) EntityManager#delete} operation.
	 	  
	 * <pre>
	 *   EntityManagerHelper.beginTransaction();
	 *   WcUserDAO.delete(entity);
	 *   EntityManagerHelper.commit();
	 *   entity = null;
	 * </pre>
	   @param entity WcUser entity to delete
	 @throws RuntimeException when the operation fails
	 */
    public void delete(AlphaDealUser entity) {
    				EntityManagerHelper.log("deleting WcUser instance", Level.INFO, null);
	        try {
        	entity = getEntityManager().getReference(AlphaDealUser.class, entity.getUserId());
        	getEntityManager().getTransaction().begin();
            getEntityManager().remove(entity);
            getEntityManager().getTransaction().commit();
            			EntityManagerHelper.log("delete successful", Level.INFO, null);
	        } catch (RuntimeException re) {
        				EntityManagerHelper.log("delete failed", Level.SEVERE, re);
	            throw re;
        }
    }
    
    /**
	 Persist a previously saved WcUser entity and return it or a copy of it to the sender. 
	 A copy of the WcUser entity parameter is returned when the JPA persistence mechanism has not previously been tracking the updated entity. 
	 This operation must be performed within the a database transaction context for the entity's data to be permanently saved to the persistence
	 store, i.e., database. This method uses the {@link javax.persistence.EntityManager#merge(Object) EntityManager#merge} operation.
	 	 
	 * <pre>
	 *   EntityManagerHelper.beginTransaction();
	 *   entity = WcUserDAO.update(entity);
	 *   EntityManagerHelper.commit();
	 * </pre>
	   @param entity WcUser entity to update
	 @return WcUser the persisted WcUser entity instance, may not be the same
	 @throws RuntimeException if the operation fails
	 */
    public AlphaDealUser update(AlphaDealUser entity) {
    				EntityManagerHelper.log("updating AlphaDeaulUser instance", Level.INFO, null);
	        try {
	        	getEntityManager().getTransaction().begin();
            AlphaDealUser result = getEntityManager().merge(entity);
            getEntityManager().getTransaction().commit();
            			EntityManagerHelper.log("update successful", Level.INFO, null);
	            return result;
        } catch (RuntimeException re) {
        				EntityManagerHelper.log("update failed", Level.SEVERE, re);
	            throw re;
        }
    }
    
    /**
     * @return user or null.
     */
    public AlphaDealUser findById( String id) {
		EntityManagerHelper.log("finding AlphaDealUser instance with id: " + id, Level.INFO, null);
		EntityManager mg = EntityManagerHelper.getNewEntityManager();
        try {
        	AlphaDealUser instance = mg.find(AlphaDealUser.class, id);
            return instance;
        } catch (RuntimeException re) {
			EntityManagerHelper.log("find failed", Level.SEVERE, re);
            return null;
        } finally {
        	if (mg != null) mg.close();
        }
    }    
}