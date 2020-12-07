/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import core.game.Observation;
import core.game.StateObservation;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observable;

import ontology.Types;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

/**
 *
 * @author yuy
 */
public class Recorder {
    public FileWriter filewriter;
    public static Instances s_datasetHeader = datasetHeader();
    
    public Recorder(String filename) throws Exception{
        
        filewriter = new FileWriter(filename+".arff");
        filewriter.write(s_datasetHeader.toString());
        /*
                // ARFF File header
        filewriter.write("@RELATION AliensData\n");
        // Each row denotes the feature attribute
        // In this demo, the features have four dimensions.
        filewriter.write("@ATTRIBUTE gameScore  NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarSpeed  NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarHealthPoints NUMERIC\n");
        filewriter.write("@ATTRIBUTE avatarType NUMERIC\n");
        // objects
        for(int y=0; y<14; y++)
            for(int x=0; x<32; x++)
                filewriter.write("@ATTRIBUTE object_at_position_x=" + x + "_y=" + y + " NUMERIC\n");
        // The last row of the ARFF header stands for the classes
        filewriter.write("@ATTRIBUTE Class {0,1,2}\n");
        // The data will recorded in the following.
        filewriter.write("@Data\n");*/
        
    }
    public static void setWeight(int para,int weight,double[]feature,int index) {
    			feature[index] = para*weight;
    }
    public static int setPara(int[][]map,int Avator_x,int k) {
    	int para = 0;
    	for(int i=0;i<14;i++) {
    		if(map[Avator_x][i]==k)
    		{para=1;break;}
    	}
    	return para;
    }
    public static double[] featureExtract(StateObservation obs){
        
        //double[] feature = new double[453];  // 448 + 4 + 1(class)
    	double[] feature = new double[14];
        // 448 locations
        int[][] map = new int[32][14];
        // Extract features
        LinkedList<Observation> allobj = new LinkedList<>();
        if( obs.getImmovablePositions()!=null )
            for(ArrayList<Observation> l : obs.getImmovablePositions()) allobj.addAll(l);
        if( obs.getMovablePositions()!=null )
            for(ArrayList<Observation> l : obs.getMovablePositions()) allobj.addAll(l);
        if( obs.getNPCPositions()!=null )
            for(ArrayList<Observation> l : obs.getNPCPositions()) allobj.addAll(l);
        
        for(Observation o : allobj){
            Vector2d p = o.position;
            int x = (int)(p.x/25);
            int y= (int)(p.y/25);
            map[x][y] = o.itype;
        }
       /*for(int y=0; y<14; y++) {
            for(int x=0; x<32; x++) {
                feature[y*32+x] = map[x][y];
            }
        }*/
        //avator位置
        Vector2d Avator= obs.getAvatarPosition();
        int Avator_x = (int)Avator.x / 25;
        int index = 0;
        
       //Avator左右正上方是否有保护物
        //int defence_up = setPara(map,Avator_x,2);
        //int defence_left = setPara(map,Avator_x-1,2);
        //int defence_right = setPara(map,Avator_x+1,2);
       
        //Avator左右正上方是否有炸弹,炸弹距离
        int bombDis = 100;
        int bomb_up=0;
        for(int i=0;i<14;i++) {
        	if(map[Avator_x][i]==5) {
        		if(12-i<bombDis)	bombDis = 12-i;	
        		bomb_up=1;
        		break;
        	}
        }
        int bomb_left = setPara(map,Avator_x-1,5);
        int bomb_right = setPara(map,Avator_x+1,5);

        //敌人数目以及上方敌人距离
        int enemyNum = 0;
        int enemyDis = 100;
        for(int i=0;i<32;i++) 
        	for(int j=13;j>=0;j--) 
        		if(map[i][j]==6) {
        			if(12-j<enemyDis) {
        				enemyDis = 12-j;
        			}
        			enemyNum+=1;
        		}
        //左右上方是否有敌人
        int enemy_up=setPara(map,Avator_x,6);
        int enemy_left=setPara(map,Avator_x-1,6);
        int enemy_right=setPara(map,Avator_x+1,6);
        
        //setWeight(defence_up,4,feature,index);index+=4;
        //setWeight(defence_left,4,feature,index);index+=4;
        //setWeight(defence_right,4,feature,index);index+=4;
        /*setWeight(bomb_up,8,feature,index);index++;
        setWeight(bomb_left,4,feature,index);index++;
        setWeight(bomb_right,4,feature,index);index++;
        setWeight(enemy_up,16,feature,index);index++;
        setWeight(enemy_left,8,feature,index);index++;
        setWeight(enemy_right,8,feature,index);index++;*/
        feature[index++] = bomb_up*8;
        feature[index++] = bomb_left*4;
        feature[index++] = bomb_right*4;
        feature[index++] = enemy_up*16;
        feature[index++] = enemy_left*8;
        feature[index++] = enemy_right*8;
        feature[index++] = enemyNum*32;
        feature[index++] = enemyDis*32;
        feature[index++] = bombDis*32;
        // 4 states
        feature[index++] = obs.getGameTick()/100;

        feature[index++] = obs.getAvatarSpeed();
        feature[index++] = obs.getAvatarHealthPoints();
        feature[index++] = obs.getAvatarType();
        
        return feature;
    }

    public static void setAttr(String s,int weight,FastVector attInfo) {
    	for(int i=0;i<weight;i++) {
    		Attribute att = new Attribute(s+i);
    		attInfo.addElement(att);
    	}
    }
    public static void setAttr(String s,FastVector attInfo) {
    	Attribute att = new Attribute(s);
    	attInfo.addElement(att);
    }
    public static Instances datasetHeader(){
        FastVector attInfo = new FastVector();
        // 448 locations
        /*for(int y=0; y<14; y++){
            for(int x=0; x<32; x++){
                Attribute att = new Attribute("object_at_position_x=" + x + "_y=" + y);
                attInfo.addElement(att);
            }
        }*/
        //setAttr("defence up",4,attInfo);
        //setAttr("defence left",4,attInfo);
        //setAttr("defence right",4,attInfo);
        setAttr("bomb up",attInfo);
        setAttr("bomb left",attInfo);
        setAttr("bomb right",attInfo);
        setAttr("enemy up",attInfo);
        setAttr("enemy left",attInfo);
        setAttr("enemy right",attInfo);
        setAttr("enemy num",attInfo);
        setAttr("enemy distance",attInfo);
        setAttr("bomb distance",attInfo);
        Attribute att = new Attribute("GameTick" ); attInfo.addElement(att);
        //Attribute att = new Attribute("Enemy Distance" ); attInfo.addElement(att);
        //att = new Attribute("Bomb Distance" ); attInfo.addElement(att);
        att = new Attribute("AvatarSpeed" ); attInfo.addElement(att);
        att = new Attribute("AvatarHealthPoints" ); attInfo.addElement(att);
        att = new Attribute("AvatarType" ); attInfo.addElement(att);
        //class
        FastVector classes = new FastVector();
        classes.addElement("0");
        classes.addElement("1");
        classes.addElement("2");
        classes.addElement("3");
        att = new Attribute("class", classes);        
        attInfo.addElement(att);
        
        Instances instances = new Instances("AliensData", attInfo, 0);
        instances.setClassIndex( instances.numAttributes() - 1);
        
        return instances;
    }
    // Record each move as the ARFF instance
    public void invoke(StateObservation obs, Types.ACTIONS action) {
        double[]  feature = featureExtract(obs);
        
        try{  
            for(int i=0; i<feature.length-1; i++)
                filewriter.write(feature[i] + ",");
            // Recorde the move type as ARFF classes
            int action_num = 0;
            if( Types.ACTIONS.ACTION_NIL == action) action_num = 0;
            if( Types.ACTIONS.ACTION_USE == action) action_num = 1;
            if( Types.ACTIONS.ACTION_LEFT == action) action_num = 2;
            if( Types.ACTIONS.ACTION_RIGHT == action) action_num = 3;
            filewriter.write(action_num + "\n");
            filewriter.flush();
        }catch(Exception exc){
            exc.printStackTrace();
        }
    }
    
    public void close(){
        try{
            filewriter.close();
        }catch(Exception exc){
            exc.printStackTrace();
        }
    }
}
