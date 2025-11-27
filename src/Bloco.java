public class Bloco {
    public int offset;    // offset em bytes
    public int order;     // ordem: 0 = 1KB, 1 = 2KB, ..., MAX_ORDER = 12 -> 4MB
    public boolean free;
    public String owner;  // "" quando está livre
    public int requestedSize; // tamanho real pedido (em bytes), 0 se livre
    public Bloco next;    // ponteiro para next na free list

    public Bloco(int off, int ord) {
        offset = off;
        order = ord;
        free = true;
        owner = "";
        requestedSize = 0;
        next = null;
    }

    public int sizeBytes(int minBlock) {
        return minBlock << order; // minBlock * 2^order
    }
}
