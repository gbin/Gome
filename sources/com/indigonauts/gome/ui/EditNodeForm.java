package com.indigonauts.gome.ui;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextField;

import com.indigonauts.gome.common.Rectangle;
import com.indigonauts.gome.sgf.Board;
import com.indigonauts.gome.sgf.SgfNode;

class EditNodeForm extends Form implements Showable {
  //#ifdef DEBUG
  //private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("EditNodeForm");
  //#endif

  TextField comment;

  public EditNodeForm(String title, Board board, SgfNode currentNode) {
    super(title);
    comment = new TextField("", currentNode.getComment(), 200, TextField.INITIAL_CAPS_SENTENCE);
    int minimumHeight = comment.getMinimumHeight();
    comment.setPreferredSize(getWidth(),minimumHeight);
    
    int h = this.getHeight() - (minimumHeight * 3)/2;
    int w = this.getWidth();
    int s = h > w ? w : h;
    BoardPainter bp = new BoardPainter(board, new Rectangle(0, 0, s, s), board.getFullBoardArea(), false);
    
    Image img = Image.createImage(s, s);
    bp.drawMe(img.getGraphics(),null,1,false,true,currentNode,null);
     
    ImageItem imageItem = new ImageItem("",img,Item.LAYOUT_CENTER,"");
    append(imageItem);
    append(comment);
    
  }

  public void show(Display destination) {
    destination.setCurrent(this);
    destination.setCurrentItem(comment);
    
  }

  public String getComment() {
    return comment.getString();
  }

}
