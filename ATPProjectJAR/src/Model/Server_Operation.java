package Model;

import Client.*;
import IO.MyDecompressorInputStream;
import Server.*;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.MyMazeGenerator;
import algorithms.search.AState;
import algorithms.search.Solution;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Server_Operation {
    private Server mazeGeneratingServer;
    private int Size_Of_Row;
    private int Size_Of_Col;
    private Maze maze;
    private Server solveSearchProblemServer;
    private Solution solve;

    public Server_Operation(int row,int col){
        this.Size_Of_Row=row;
        this.Size_Of_Col=col;
    }

        public Maze Generate_Operate() {
            mazeGeneratingServer = new Server(5400, 1000, new ServerStrategyGenerateMaze());
            mazeGeneratingServer.start();
            CommunicateWithServer_MazeGenerating();
            mazeGeneratingServer.stop();
            return this.maze;
        }

        public Solution Solve_operate(){

            solveSearchProblemServer = new Server(5401, 1000, new ServerStrategySolveSearchProblem());
            solveSearchProblemServer.start();
            CommunicateWithServer_SolveSearchProblem();
            solveSearchProblemServer.stop();
            return this.solve;

        }

        private void CommunicateWithServer_MazeGenerating() {
            try {
                Client client = new Client(InetAddress.getLocalHost(), 5400, new IClientStrategy() {
                    public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                        try {
                            ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                            ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                            toServer.flush();
                            int[] mazeDimensions = new int[]{getSize_Of_Row(),getSize_Of_Col()};
                            toServer.writeObject(mazeDimensions);
                            toServer.flush();
                            byte[] compressedMaze = (byte[])((byte[])fromServer.readObject());
                            InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                            byte[] decompressedMaze = new byte[100000];
                            is.read(decompressedMaze);
                            Maze maze = new Maze(decompressedMaze);
                            setMaze(maze);

                        } catch (Exception var10) {
                            var10.printStackTrace();
                        }

                    }
                });
                client.communicateWithServer();
            } catch (UnknownHostException var1) {
                var1.printStackTrace();
            }

        }

    private void CommunicateWithServer_SolveSearchProblem() {
        try {
            Client client = new Client(InetAddress.getLocalHost(), 5401, new IClientStrategy() {
                public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        toServer.writeObject(getMaze());
                        toServer.flush();
                        Solution mazeSolution = (Solution)fromServer.readObject();
                        setSolve(mazeSolution);


                    } catch (Exception var10) {
                        var10.printStackTrace();
                    }

                }
            });
            client.communicateWithServer();
        } catch (UnknownHostException var1) {
            var1.printStackTrace();
        }

    }


    public int getSize_Of_Row() {
        return Size_Of_Row;
    }

    public void setSize_Of_Row(int size_Of_Row) {
        Size_Of_Row = size_Of_Row;
    }

    public int getSize_Of_Col() {
        return Size_Of_Col;
    }

    public void setSize_Of_Col(int size_Of_Col) {
        Size_Of_Col = size_Of_Col;
    }

    public Maze getMaze() {
        return maze;
    }

    public void setMaze(Maze maze) {
        this.maze = maze;
    }

    public Solution getSolve() {
        return solve;
    }

    public void setSolve(Solution solve) {
        this.solve = solve;
    }
}
