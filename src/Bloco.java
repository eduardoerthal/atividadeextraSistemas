//essa classe representa a estrutura que representa um bloc de memoria dentro do buddy

public class Bloco {
    public int offset; // variavel de começo da memoria
    public int order; // 0 = menor bloco possivel (1k)
    public boolean free; //logica para trabalhar com livre e ocupado
    public String owner; // rotulo do programa alocado atual

    public Bloco(int off, int ord) {
        offset = off;
        order = ord;
        free = true;
        owner = "";
    }

    public int getSize(int minSize){
        return (1 << order) * minSize; // calcula o 2^ order * min bloco
    }
    public int buddyOffset(int minSize){
        return offset ^ getSize(minSize); //xor compara dois valores binarios vit a bit para encontrar o irmao (buddy)
    }
}