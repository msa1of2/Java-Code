package helpful.datastructures;

import java.awt.Graphics;
import java.awt.Color;
public class BinarySearchTree<E extends Comparable<E>>{
    private TreeNode<E> root;
    public BinarySearchTree(){
        root = new TreeNode<E>(null);
    }
    public void add(E data){
        if (root == null){
            root = new TreeNode<E>(data);
            return;
        }
        if (root.get() == null){
            root.set(data);
            return;
        }
        add(data, root);
        boolean balanced = isBalanced(root);
        if (balanced){
            return; //dont go though a mess of logic, just exit
        }
        rebalance(root);
    }
    public void add(E[] data){
        for (int i=0;i<data.length;i++){
            add(data[i]);
        }
    }
    private void add(TreeNode<E> current){
        if (root == null){
            root = current;
            return;
        }
        add(current, root);
        rebalance(root);
    }
    private void add(E data, TreeNode<E> node){
        if (data.compareTo(node.get()) < 0){
            if (node.getLeft() == null ? true : node.getLeft().get() == null){
                node.setLeft(data);
            }
            else{
                add(data, node.getLeft());
            }
        }
        if (data.compareTo(node.get()) > 0){
            if (node.getRight() == null ? true : node.getRight().get() == null){
                node.setRight(data);
            }
            else{
                add(data, node.getRight());
            }
        }
    }
    private void add(TreeNode<E> current, TreeNode<E> node){
        if (current.get().compareTo(node.get()) < 0){
            if (node.getLeft() == null ? true : node.getLeft().get() == null){
                node.setLeftNode(current);
            }
            else{
                add(current, node.getLeft());
            }
        }
        if (current.get().compareTo(node.get()) > 0){
            if (node.getRight() == null ? true : node.getRight().get() == null){
                node.setRightNode(current);
            }
            else{
                add(current, node.getRight());
            }
        }
    }
    public void rebalance(){
        rebalance(root);
    }
    private int rebalance(TreeNode<E> node){
        if (node == null){
            //System.out.println("Node is null or has no kids");
            return -100;
        }
        if (isLeaf(node)){
            return 0;
        }
        int result = 0;
        boolean leftBalanced = isBalanced(node.getLeft()), rightBalanced = isBalanced(node.getRight());
        if (!rightBalanced){
            result = rebalance(node.getRight()) + 1;
        }
        if (!leftBalanced){
            result = rebalance(node.getLeft()) + 1;
        }
        if (isBalanced(node)){
            //System.out.println("Node: " + node + " is balanced");
            return result;
        }
        int leftHeight = getHeight(node.getLeft());
        int rightHeight = (getHeight(node.getRight()));
        int level = getHeight(node);
        if (Math.abs(leftHeight-rightHeight) == 2){
            //distance of 2 from a leaf, 2 height above the other side
            if (level == 4){
                //System.out.println("Solve 2 on: " + node);
                if (rightHeight > leftHeight){
                    if (getHeight(node.getRight().getRight()) > getHeight(node.getRight().getLeft())){
                        //rotate right
                        //System.out.println("Rotate right, basically 1A");
                        rotateLeft(node);
                    }
                    else{
                        //rotate left
                        //System.out.println("Rotate right, 2B");
                        rotateRight(node.getRight());
                        rotateLeft(node);
                    }
                }
                else{
                    if (getHeight(node.getLeft().getRight()) > getHeight(node.getLeft().getLeft())){
                        //rotate left
                        //System.out.println("Rotate left, basically 1A");
                        rotateRight(node);
                    }
                    else{
                        //roate right
                        //System.out.println("Rotate left, 2B");
                        rotateLeft(node.getLeft());
                        rotateRight(node);
                    }
                }
                return result;
                //solve2A(node);
            }
            boolean should2B = false;
            if (leftHeight > rightHeight ? node.getLeft().getLeft() == null : node.getRight().getRight() == null){
                should2B=true;
            }
            if (should2B){
                //System.out.println("Solve 1B on: " + node);
                solve1B(node);
            }
            else{
                //System.out.println("Solve 1A on: " + node);
                solve1A(node);
            }
            
        }
        
        //System.out.println("Node: " + node + ", Left Height: " + leftHeight + ", Right Height: " + rightHeight + ", Left Balanced: " + leftBalanced + ", Right Balanced: " + rightBalanced + ", Level: " + level + ", Result: " + result);
        return result;

    }
    public void remove(E data){
        if (root == null ? true : root.get() == null){
            return;
        }
        remove(data, root);
    }
    private boolean remove(E data, TreeNode<E> node){
        if (node == null){
            return false;
        }
        if (data.compareTo(node.get()) > 0){
            return remove(data, node.getRight()); //remove was done, we can leave
        }
        else if (data.compareTo(node.get()) < 0){
            return remove(data, node.getLeft()); //remove was done, we can leave
        }
        //data is now the same as node's data


        if ((node.getLeft() != null ? node.getLeft().get() != null : false) && (node.getRight() != null ? node.getRight().get() != null : false)){
            //both children are not null
            TreeNode<E> lowest = node.getRight();
            boolean looped = false;
            while (lowest.getLeft() != null ? lowest.getLeft().get() != null : false){
                lowest = lowest.getLeft();
                looped=true;
            }
            //lowest is now the lowest value node (Farthest left) in the right subtree
            node.set(lowest.get());

            if (looped){
                lowest.getParent().setLeftNode(lowest.getRight()); //might have right sub node(s) with no left node
            }
            else{
                lowest.getParent().setRightNode(lowest.getRight());
            }
            //since lowest is farthest left, it is smaller than lowestParent, which means the right of lowest is smaller than lowest parent else it would have gone right not left
            return true; //remove was done, we can leave
        }
        if (node.getLeft() != null ? node.getLeft().get() != null : false){
            //left child is not null, we don't know where node is relative to parent
            if (node.getParent() == null){
                //root node is the node variable
                root = root.getLeft(); //move the tree down to the left node
                root.setParentNode(null); //remove a non-existant parent
                return true; //remove was done, we can leave
            }
            if (node.getParent().getLeft() == node){
                //node is the left child of parent
                node.getParent().setLeftNode(node.getLeft());
            }
            if (node.getParent().getRight() == node){
                //node is the right child of parent
                node.getParent().setRightNode(node.getLeft());
            }
            return true; // remove was done, we can leave
        }
        if (node.getRight() != null ? node.getRight().get() != null : false){
            //right child is not null, we don't know where node is relative to parent
            if (node.getParent() == null){ //only true for root node
                root = root.getRight();
                root.setParentNode(null);
                return true; //remove was done, we can leave
            }
            if (node.getParent().getLeft() == node){
                //node is the left child of parent
                node.getParent().setLeftNode(node.getRight());
            }
            if (node.getParent().getRight() == node){
                //node is the right child of parent
                node.getParent().setRightNode(node.getRight());
            }
            return true; //remove was done, we can leave
        }
        //node has no children
        if (node.getParent() == null){
            //root node is the node variable
            root = null;
            return true;
        }
        if (node.getParent().getLeft() == node){
            node.getParent().setLeftNode(null);
            return true;
        }
        if (node.getParent().getRight() == node){
            node.getParent().setRightNode(null);
            return true;
        }
        return false;
    }
    private String inOrderString(TreeNode<E> node){
        if(node == null ? true : node.get() == null){
            return "";
        }
        String send = "";
        send += inOrderString(node.getLeft());
        send += node.get() + ", ";
        send += inOrderString(node.getRight());
        return send;
    }
    public String toStringPreOrder(){
        return toStringPreOrder(root);
    }
    private String toStringPreOrder(TreeNode<E> node){
        if (node == null ? true : node.get() == null){
            return "";
        }
        String send = "";
        send += node.get() + ", ";
        send += toStringPreOrder(node.getLeft());
        send += toStringPreOrder(node.getRight());
        return send;
    }
    public int getHeight(){
        return getHeight(root);
    }
    public int getHeight(TreeNode<E> node){
        if (node == null ? true : node.get() == null || !contains(node.get(), root)){
            return 0;
        }
        return 1 + Math.max(getHeight(node.getLeft()), getHeight(node.getRight()));
    }
    public boolean isBalanced(){
        return isBalanced(root);
    }
    public boolean isBalanced(TreeNode<E> node){
        if (node == null ? true : node.get() == null){
            return true;
        }
        return Math.abs(getHeight(node.getLeft()) - getHeight(node.getRight())) <= 1 
            && isBalanced(node.getLeft()) 
            && isBalanced(node.getRight());
    }
    public boolean contains(E data){
        return contains(data, root);
    }
    private boolean contains(E data, TreeNode<E> node){
        if (node == null ? true : node.get() == null){
            return false;
        }
        if (data.compareTo(node.get()) > 0){
            return contains(data, node.getRight());
        }
        if (data.compareTo(node.get()) < 0){
            return contains(data, node.getLeft());
        }
        //data is either less than, greater than or equal to node's data
        //to get here it cant be less than or greater than so it's equal
        return true;
    }
    public void rotateRight(TreeNode<E> node){
        if (node == null){
            return;
        }
        //node is the parent node of the branching nodes where imbalance occurs
        TreeNode<E> tempParent = node.getParent();
        //left has more nodes
        TreeNode<E> temp = node.getLeft();
        if (temp == null){
            return;
        }
        if (tempParent == null){
            root = temp;
            temp.setParentNode(null);
        }
        else if (tempParent.getLeft() == node){
            //left of parent
            tempParent.setLeftNode(temp);
        }
        else if (tempParent.getRight() == node){
            //right of parent
            tempParent.setRightNode(temp);
        }

        node.setLeftNode(temp.getRight());
        temp.setRightNode(node);
    }
    public void rotateLeft(TreeNode<E> node){
        if (node == null){
            return;
        }
        TreeNode<E> tempParent = node.getParent();
        TreeNode<E> temp = node.getRight();
        if (temp == null){
            return;
        }
        if (tempParent == null){
            //root is node
            root = temp;
            temp.setParentNode(null);
        }
        else if (tempParent.getLeft() == node){
            //left of parent
            tempParent.setLeftNode(temp);
        }
        else if (tempParent.getRight() == node){
            //right of parent
            tempParent.setRightNode(temp);
        }

        node.setRightNode(temp.getLeft());
        temp.setLeftNode(node);
    }
    private void solve1A(TreeNode<E> node){
        //node is the one in the middle according to 1A diagram
        if (node == null ? true : node.get() == null){
            return;
        }
        int leftHeight = getHeight(node.getLeft());
        int rightHeight = getHeight(node.getRight());
        if (leftHeight > rightHeight){
            rotateRight(node);
        }
        if (rightHeight > leftHeight){
            rotateLeft(node);
        }

    }
    private void solve1B(TreeNode<E> node){
        //node is the one in the middle according to 1B diagram
        if (node == null ? true : node.get()==null){
            return;
        }
        int leftHeight = getHeight(node.getLeft());
        int rightHeight = getHeight(node.getRight());
        if (leftHeight < rightHeight){
            rotateRight(node.getRight());
        }
        else{
            rotateLeft(node.getLeft());
        }
        solve1A(node);
    }
    private void solve2A(TreeNode<E> node){
        if (node == null ? true : node.get() == null){
            return;
        }
        int leftHeight = getHeight(node.getLeft());
        int rightHeight = getHeight(node.getRight());
        if (rightHeight > leftHeight){
            rotateLeft(node);
        }
        else{
            rotateRight(node);
        }
    }
    public String toString(){
        return inOrderString(root);
    }
    public void clear(){
        clear(root);
        root.set(null);
    }
    private void clear(TreeNode<E> node){
        if (node == null ? true : node.get() == null){
            return;
        }
        clear(node.getLeft());
        clear(node.getRight());
        node.setLeftNode(null);
        node.setRightNode(null);
        node.setParentNode(null);
    }
    public TreeNode<E> getRoot(){
        return root;
    }
    public boolean isLeaf(TreeNode<E> node){
        return (node.getLeft() == null && node.getRight() == null);
    }
    public int getLevel(TreeNode<E> node){
        if (node == null ? true : node.get() == null){
            return -1;
        }
        return getLevel(node.getParent()) + 1;
    }
	public void draw(Graphics g, int xSize, int y){
        //takes in graphics object, maximum size of screen in xSize and the y-offset
        //set max height as 5 for now
        int height = Math.min(this.getHeight(root), 5);
        drawTreeRecursive(root, g, xSize/2, y+25, 0, xSize, 0);
    }
    private void drawTreeRecursive(TreeNode<E> node, Graphics g, int x, int y, int depth, int xSize, int xStart){
        if (depth >= 5 || (node != null ? node.get() == null : true)){
            return;
        }
        //Debug coloring
        //g.setColor(new Color((int)(Math.pow(x,2)*5%255), (int)(Math.pow(y,2)*7%255), depth*50));
        //g.fillRect(xStart, depth*75, xSize, 75);

        g.setColor(Color.lightGray);
        g.fillOval(x-25, y-25, 50, 50);
        g.setColor(Color.black);
        g.drawString(node.get().toString(), x-g.getFontMetrics().stringWidth(node.get().toString())/2, y-3+g.getFontMetrics().getHeight()/2);
        if (node.getLeft() != null){
            g.drawLine(x-18,y+18, xSize/(4)+xStart+18,y+62);
            drawTreeRecursive(node.getLeft(), g, xSize/(4)+xStart, y+75, depth+1, xSize/2, xStart);
        }
        if (node.getRight() != null){
            g.drawLine(x+18, y+18, 3*xSize/(4)+xStart-18, y+62);
            drawTreeRecursive(node.getRight(), g, 3*xSize/4+xStart, y+75, depth+1, xSize/2, xStart + xSize/2);
        }
    }
	 public static class TreeNode<E>{
	    private E data;
	    private TreeNode<E> left, right, parent;
	    public TreeNode(E data){
	        this.data = data;
	        left = right = parent = null; //this works!
	    }
	    public E get(){
	        return data;
	    }
	    public void set(E data){
	        this.data = data;
	    }
	    public TreeNode<E> getLeft(){
	        return left;
	    }
	    public TreeNode<E> getRight(){
	        return right;
	    }
	    public TreeNode<E> getParent(){
	        return parent;
	    }
	    public void setLeftNode(TreeNode<E> left){
	        this.left = left;
	        if (left != null){
	            this.left.setParentNode(this);
	        }
	    }
	    public void setLeft(E data){
	        setLeftNode(new TreeNode<E>(data)); //so changes can be made easily
	    }
	    public void setParentNode(TreeNode<E> parent){
	        this.parent = parent;
	    }
	    public void setRightNode(TreeNode<E> right){
	        this.right = right;
	        if (right != null){
	            this.right.setParentNode(this);
	        }
	    }
	    public void setRight(E data){
	        setRightNode(new TreeNode<E>(data));
	    }
	    public String toString(){
	        return data.toString();
	    }
	}
}