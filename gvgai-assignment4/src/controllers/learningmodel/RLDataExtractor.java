/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers.learningmodel;

import tools.*;
import core.game.Observation;
import core.game.StateObservation;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Observable;

import ontology.Types;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author yuy
 */
public class RLDataExtractor {
    public FileWriter filewriter;
    public static Instances s_datasetHeader = datasetHeader();
    
    public RLDataExtractor(String filename) throws Exception{
        
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
    
    public static Instance makeInstance(double[] features, int action, double reward){
        features[178] = action;
        features[179] = reward;
        Instance ins = new Instance(1, features);
        ins.setDataset(s_datasetHeader);
        return ins;
    }
    
    public static double[] featureExtract(StateObservation obs){
    	Vector2d avatar = obs.getAvatarPosition();
        int avatar_x = (int)avatar.x / 28;
        int avatar_y = (int)avatar.y / 28;
        int index=0;
        
        Vector2d []blocks = new Vector2d[100];
        int block_cnt = 0;
        int highest_block = 15;
        
        Vector2d []wedges = new Vector2d[100];
        int wedge_cnt = 0;
        int highest_wedge = 15;
        
        //added feautures
    	double xdis2portal = 0;
    	double ydis2portal = 0;
    	double xdis2gap = 100;
    	double ydis2gap = 100;
    	double xdis2wedge = 100;
    	double ydis2wedge = 100;
    	
        double[] feature = new double[180];  // 6*28(168) + 6 + 4 + 1(action) + 1(Q)
        
        // 448 locations
        int[][] map = new int[28][15];
        // Extract features
        LinkedList<Observation> allobj = new LinkedList<>();
        if(obs.getPortalsPositions()!=null)
        	for(ArrayList<Observation> l:obs.getPortalsPositions())	allobj.addAll(l);
        if( obs.getImmovablePositions()!=null )
            for(ArrayList<Observation> l : obs.getImmovablePositions()) allobj.addAll(l);
        if( obs.getMovablePositions()!=null )
            for(ArrayList<Observation> l : obs.getMovablePositions()) allobj.addAll(l);
        if( obs.getNPCPositions()!=null )
            for(ArrayList<Observation> l : obs.getNPCPositions()) allobj.addAll(l);
        if( obs.getResourcesPositions()!=null )
            for(ArrayList<Observation> l : obs.getResourcesPositions()) allobj.addAll(l);
        
        for(Observation o : allobj){
            Vector2d p = o.position;
            int x = (int)(p.x/28); //squre size is 20 for pacman
            int y= (int)(p.y/28);  //size is 28 for FreeWay
            map[x][y] = o.itype;
            
            if(o.itype==4) {
            	//set property dis2portal
            	xdis2portal = Math.abs(x-avatar_x);
            	ydis2portal = Math.abs(y-avatar_y);
            }
            if(o.itype==13) {
            	//block
            	blocks[block_cnt] = o.position;
            	block_cnt++;
            	if(y<highest_block)	highest_block = y;
            }
            if(o.itype==7||o.itype==8||o.itype==10||o.itype==11) {
            	wedges[wedge_cnt] = o.position;
            	wedge_cnt++;
            	if(y<highest_wedge)	highest_wedge = y;
            }
            
        }
        //find the mindistance to gap
        if(avatar_y<highest_block) {
    		xdis2gap = 0;
    		ydis2gap = 0;
    		
    	}
        else {
        for(int i=0;i<block_cnt;i++) {
        	if(blocks[i].y<avatar_y) {
        		if(map[(int)blocks[i].x-1][(int)blocks[i].y]!=13||map[(int)blocks[i].x+1][(int)blocks[i].y]!=13)
        		if(Math.abs((int)blocks[i].y-avatar_y)+Math.abs((int)blocks[i].x-avatar_x)<xdis2gap+ydis2gap) {
        			xdis2gap = Math.abs((int)blocks[i].x-avatar_x);
        			ydis2gap = Math.abs((int)blocks[i].y-avatar_y);
        		}
        	}
        }
        }
        //find the mindistance to wedge
        if(avatar_y<highest_wedge) {
        	xdis2wedge = 0;
        	ydis2wedge = 0;
        }
        else {
        	for(int i=0;i<wedge_cnt;i++) {
        		if(wedges[i].y<avatar_y) {
        			if(Math.abs((int)wedges[i].y-avatar_y)+Math.abs((int)wedges[i].x-avatar_x)<xdis2wedge+ydis2wedge) {
            			xdis2wedge = Math.abs((int)wedges[i].x-avatar_x);
            			ydis2wedge = Math.abs((int)wedges[i].y-avatar_y);
            		}
        		}
        	}
        }
        
        //6行信息
        int high_y = 4;
        if(avatar_y>4)	high_y = avatar_y;
        for(int y=high_y-1;y>=high_y-4;y--){
        	for(int x=0;x<28;x++) {
        		feature[index++] = map[x][y];
        	}
        }
        int low_y = 13;
        if(avatar_y<13)	high_y = avatar_y;
        for(int y=low_y;y<low_y+2;y++) {
        	for(int x=0;x<28;x++) {
        		feature[index++] = map[x][y];
        	}
        }
        //System.out.println(index);
        /*for(int y=0; y<31; y++)
            for(int x=0; x<28; x++)
                feature[y*28+x] = map[x][y];
        */
        // 4 states
        feature[index++] = xdis2portal;
        feature[index++] = ydis2portal;
        feature[index++] = xdis2gap;
        feature[index++] = ydis2gap;
        feature[index++] = xdis2wedge;
        feature[index++] = ydis2wedge;
        feature[index++] = obs.getGameTick();
        feature[index++] = obs.getAvatarSpeed();
        feature[index++] = obs.getAvatarHealthPoints();
        feature[index++] = obs.getAvatarType();
        
        return feature;
    }
    
    public static Instances datasetHeader(){
        
        if (s_datasetHeader!=null)
            return s_datasetHeader;
        
        FastVector attInfo = new FastVector();
        // 448 locations
        for(int y=0; y<6; y++){
            for(int x=0; x<28; x++){
                Attribute att = new Attribute("object_at_position_x=" + x + "_y=" + y);
                attInfo.addElement(att);
            }
        }
        Attribute att = new Attribute("xdis2portal" ); attInfo.addElement(att);
        att = new Attribute("ydis2portal" ); attInfo.addElement(att);
        att = new Attribute("xdis2gap" ); attInfo.addElement(att);
        att = new Attribute("ydis2gap" ); attInfo.addElement(att);
        att = new Attribute("xdis2wedge" ); attInfo.addElement(att);
        att = new Attribute("ydis2wedge"); attInfo.addElement(att);
       
        att = new Attribute("GameTick" ); attInfo.addElement(att);
        att = new Attribute("AvatarSpeed" ); attInfo.addElement(att);
        att = new Attribute("AvatarHealthPoints" ); attInfo.addElement(att);
        att = new Attribute("AvatarType" ); attInfo.addElement(att);
        //action
        FastVector actions = new FastVector();
        actions.addElement("0");
        actions.addElement("1");
        actions.addElement("2");
        actions.addElement("3");
        att = new Attribute("actions", actions);        
        attInfo.addElement(att);
        // Q value
        att = new Attribute("Qvalue");
        attInfo.addElement(att);
        
        Instances instances = new Instances("PacmanQdata", attInfo, 0);
        instances.setClassIndex( instances.numAttributes() - 1);
        
        return instances;
    }
    
}
