public class Buddy{

    public static final int TOTAL_MEM = 4 * 1024 * 1024;    // 4MB para a memoria toatl
    public static final int MIN_BLOCK = 1024;               // 1KB bloco minimo esturutado
    public static final int MAX_ORDER = 12;                 // 4MB = 2^12 KB maximo 

    // estrutura interna 
    
    private Bloco[] freeList = new Bloco[1000];
    private int freeCount = 0;

    // Lista de blocos ocupados
    private Bloco[] used = new Bloco[1000];
    private int usedCount = 0;

    public Buddy() {
        // insere bloco inicial de 4MB (ordem máxima)
        freeList[0] = new Bloco(0, MAX_ORDER);
        freeCount = 1;
    }

    // função para converter o tamanho para potencia de 2
    private int sizeToOrder(int size) {
        int ord = 0;
        int bloco = MIN_BLOCK;

        while (bloco < size) {
            bloco = bloco * 2;
            ord = ord + 1;
        }
        return ord;
    }
    

    // first fit
    public boolean allocate(String name, int size) {

        int target = sizeToOrder(size);

        int index = acharBlocoLivre(target);
        if (index < 0) return false;  // erro de falta de espaço sem exception

        Bloco b = freeList[index];
        removeFree(index);

        while (b.order > target) {
            b = splitBloco(b);
        }

        b.free = false;
        b.owner = name;
        used[usedCount] = b;
        usedCount++;

        return true;
    }

    // localiza o bloco adequado para o first-fit
    private int acharBlocoLivre(int needOrder) {
        int i = 0;
        while (i < freeCount) {
            if (freeList[i].order >= needOrder) return i;
            i = i + 1;
        }
        return -1;
    }

    // split buddy
    private Bloco splitBloco(Bloco b) {

        int newOrd = b.order - 1;
        int half = (1 << newOrd) * MIN_BLOCK;

        Bloco b1 = new Bloco(b.offset, newOrd);
        Bloco b2 = new Bloco(b.offset + half, newOrd);

        insertFree(b1);
        insertFree(b2);

        return b1; // devolve um pedaço para continuar alocação
    }

    // inserir e remover do free list
    private void insertFree(Bloco b) {
        freeList[freeCount] = b;
        freeCount++;
    }

    private void removeFree(int idx) {
        freeCount = freeCount - 1;
        freeList[idx] = freeList[freeCount];
    }

    // report para relatorio - estrutura simples com texto
    public void printReport() {

        int i = 0;
        int totalLivre = 0;

        System.out.println("\n ======BLOCOS LIVRES======");
        while (i < freeCount){
            Bloco b = freeList[i];
            System.out.println("Offset: " + b.offset + "| Tamanho: " + b.getSize(MIN_BLOCK));
            totalLivre = totalLivre + b.getSize(MIN_BLOCK);
            i = i+1;

        }

        System.out.println("\nTotal Livre: " + totalLivre + " bytes");
        System.out.println("Fragmentação: " + freeCount + " blocos");

        System.out.println("\n=======BLOCOS OCUPADOS======");
        int j = 0;
        while (j < usedCount) {
            Bloco b = used[j];
            System.out.println(
                    "["+b.owner+"] real="+(b.getSize(MIN_BLOCK))+
                            " bloco="+b.getSize(MIN_BLOCK)+
                            " offset="+b.offset
            );
            j = j + 1;
        }
    }
}
