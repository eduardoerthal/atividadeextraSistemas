public class Buddy {

    public static final int TOTAL_BYTES = 4 * 1024 * 1024; // 4MB
    public static final int MIN_BLOCK = 1024;              // 1KB
    public static final int MAX_ORDER = 12;                // 1KB * 2^12 = 4MB

    // cabeças das listas livres por ordem evitando lenght
    private Bloco[] freeHeads = new Bloco[MAX_ORDER + 1];

    // lista simples para blocos alocados (controle manual)
    // capacidade fixa; gerenciamos com allocatedCount
    private Bloco[] allocated = new Bloco[3000];
    private int allocatedCount = 0;

    public Buddy() {
        // inicializa com um bloco livre único do tamanho máximo
        for (int i = 0; i <= MAX_ORDER; i = i + 1) {
            freeHeads[i] = null;
        }
        Bloco initial = new Bloco(0, MAX_ORDER);
        initial.free = true;
        initial.owner = "";
        initial.next = null;
        freeHeads[MAX_ORDER] = initial;
    }

    private int requiredOrder(int sizeBytes) {
        int ord = 0;
        int blockSize = MIN_BLOCK; // size for order 0
        while (blockSize < sizeBytes && ord < MAX_ORDER) {
            blockSize = blockSize << 1; // *2
            ord = ord + 1;
        }
        return ord;
    }

    private Bloco popFreeHead(int ord) {
        Bloco head = freeHeads[ord];
        if (head == null) return null;
        freeHeads[ord] = head.next;
        head.next = null;
        return head;
    }
    private void pushFreeHead(int ord, Bloco b) {
        b.next = freeHeads[ord];
        freeHeads[ord] = b;
    }

    private Bloco findAndSplit(int ordNeeded) {
        int ord = ordNeeded;
        while (ord <= MAX_ORDER) {
            if (freeHeads[ord] != null) break;
            ord = ord + 1;
        }
        if (ord > MAX_ORDER) return null;
        Bloco b = popFreeHead(ord);
        while (ord > ordNeeded) {
            ord = ord - 1;
            int size = MIN_BLOCK << ord;
            Bloco left = new Bloco(b.offset, ord);
            Bloco right = new Bloco(b.offset + size, ord);
            pushFreeHead(ord, right);
            b = left;
        }
        return b;
    }

    // alocar: retorna true se alocou, false se falhar
    public boolean allocate(String label, int sizeBytes) {
        if (sizeBytes <= 0 || sizeBytes > TOTAL_BYTES) return false;
        int ordNeeded = requiredOrder(sizeBytes);
        Bloco b = findAndSplit(ordNeeded);
        if (b == null) return false;
        b.free = false;
        b.owner = label;
        b.requestedSize = sizeBytes;
        b.next = null;
        if (allocatedCount < 3000) {
            allocated[allocatedCount] = b;
            allocatedCount = allocatedCount + 1;
            return true;
        } else {
            b.free = true;
            b.owner = "";
            pushFreeHead(b.order, b);
            return false;
        }
    }

    // busca índice do bloco alocado pelo rótulo (retorna -1 se não encontrado)
    private int findAllocatedIndexByLabel(String label) {
        int i = 0;
        while (i < allocatedCount) {
            Bloco b = allocated[i];
            if (b != null && b.owner != null && b.owner.equals(label)) {
                return i;
            }
            i = i + 1;
        }
        return -1;
    }

    // remove o bloco alocado do array allocated mantendo packed array
    private void removeAllocatedAt(int idx) {
        if (idx < 0 || idx >= allocatedCount) return;
        // move último para a posição idx
        allocatedCount = allocatedCount - 1;
        allocated[idx] = allocated[allocatedCount];
        allocated[allocatedCount] = null;
    }

    public boolean free(String label) {
        int idx = findAllocatedIndexByLabel(label);
        if (idx == -1) return false;
        Bloco b = allocated[idx];

        b.free = true;
        b.owner = "";
        int ord = b.order;
        int off = b.offset;

        while (ord < MAX_ORDER) {
            int blockSize = MIN_BLOCK << ord;
            int buddyOffset = off ^ blockSize;

            Bloco prev = null;
            Bloco cur = freeHeads[ord];
            boolean buddyFound = false;
            while (cur != null) {
                if (cur.offset == buddyOffset) {
                    if (prev == null) {
                        freeHeads[ord] = cur.next;
                    } else {
                        prev.next = cur.next;
                    }
                    cur.next = null;
                    buddyFound = true;
                    break;
                }
                prev = cur;
                cur = cur.next;
            }
            if (!buddyFound) {
                b.order = ord;
                b.next = null;
                pushFreeHead(ord, b);
                removeAllocatedAt(idx);
                return true;
            } else {
                int mergedOffset = (off < buddyOffset) ? off : buddyOffset;
                off = mergedOffset;
                ord = ord + 1;
                b = new Bloco(off, ord);
                b.free = true;
                b.owner = "";
                b.requestedSize = 0;
                b.next = null;
            }
        }
        b.order = ord;
        b.offset = off;
        b.free = true;
        b.owner = "";
        b.next = null;
        pushFreeHead(ord, b);
        removeAllocatedAt(idx);
        return true;
    }

    // imprime relatório: área livre, blocos livres fragmentados, blocos alocados
    public void printReport() {
        System.out.println("=== Relatório da memória (Buddy) ===");
        // total livre
        long totalFree = 0;
        System.out.println("-- Blocos livres por ordem:");
        int ord = 0;
        while (ord <= MAX_ORDER) {
            int count = 0;
            Bloco cur = freeHeads[ord];
            while (cur != null) {
                count = count + 1;
                totalFree = totalFree + (MIN_BLOCK << ord);
                cur = cur.next;
            }
            if (count > 0) {
                System.out.println("  Ordem " + ord + " (tamanho " + (MIN_BLOCK << ord) + " bytes): " + count + " bloco(s)");
            }
            ord = ord + 1;
        }
        System.out.println("Área total livre: " + totalFree + " bytes (" + (totalFree / 1024) + " KB)");

        // listar blocos livres (tamanho e posição)
        System.out.println("-- Lista detalhada de blocos livres (offset, tamanho):");
        ord = 0;
        while (ord <= MAX_ORDER) {
            Bloco cur = freeHeads[ord];
            while (cur != null) {
                System.out.println("  offset=" + cur.offset + " size=" + (MIN_BLOCK << ord) + " bytes (ord=" + ord + ")");
                cur = cur.next;
            }
            ord = ord + 1;
        }

        // listar blocos alocados
        System.out.println("-- Blocos alocados:");
        int i = 0;
        while (i < allocatedCount) {
            Bloco b = allocated[i];
            System.out.println("  " + b.owner + " | requested=" + b.requestedSize + " bytes | blockSize=" + (MIN_BLOCK << b.order) + " bytes | offset=" + b.offset + " | ord=" + b.order);
            i = i + 1;
        }
        System.out.println("=== Fim do relatório ===");
    }
}
