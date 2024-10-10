
package cache_project;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class cache_simulator {

	public static void main(String[] args) {
		
			// LINHA 18 A 31 - CÓDIGO UTILIZADO PARA RODAR VIA TERMINAL:
		/* 
		if (args.length != 6){
            //System.out.println(args.length);
            System.out.println("Numero de argumentos incorreto. Utilize:");
            System.out.println("java cache_simulator <nsets> <bsize> <assoc> <substituição> <flag_saida> arquivo_de_entrada");
            System.exit(1);
        }
        int nsets = Integer.parseInt(args[0]);
        int bsize = Integer.parseInt(args[1]);
        int assoc = Integer.parseInt(args[2]);
        String subst = args[3];
        int flagOut = Integer.parseInt(args[4]);
        String arquivoEntrada = args[5];
        */
        
		
			// LINHA 36 A 46 - CÓDIGO UTILIZADO PARA RODAR VIA ECLIPSE:
		// lendo a entrada no console
		Scanner sc = new Scanner(System.in);
		
		String name = sc.next();
		int nsets = sc.nextInt();
        int bsize = sc.nextInt();
        int assoc = sc.nextInt();
        String subst = sc.next();
        int flagOut = sc.nextInt();
        String arquivoEntrada = sc.next();
		
		sc.close();
		
		
			// Iniciando as variaveis:
        int hit = 0;
        int miss = 0;
        int acessos = 0;
        int miss_compulsorio = 0;
        int miss_conflito = 0;
        int miss_capacidade = 0;
		int value = 0;
		Random aleatorio = new Random();


        
       		// número de informações a serem armazenadas
        int[] cache_val = new int[nsets * assoc];
		int[] cache_tag = new int[nsets * assoc];
		List<Integer> lru_priority = new ArrayList<>();
		
		List<Integer>[] lru_priority_conj = new ArrayList[nsets];
		for(int i=0; i<nsets; i++) {
			lru_priority_conj[i] = new ArrayList<Integer>();
		}
		
		
			// calculando os bits de offset, índice e tag
		int n_bits_offset = (int)(Math.log(bsize)/Math.log(2));
		int n_bits_indice = (int)(Math.log(nsets)/Math.log(2));
		int n_bits_tag = 32 - n_bits_offset - n_bits_indice;

		
		try (BufferedInputStream br = new BufferedInputStream(new FileInputStream(arquivoEntrada))) {
		
			int x, bytes;
			while((x = br.read()) != -1) {
				bytes = x;
				for(int i=0; i<3; i++) {
					bytes = bytes << 8;
					x = br.read();
					bytes = bytes | x;
				}
				int endereco = bytes;
				int tag = endereco >> (n_bits_offset + n_bits_indice);
				int indice = (endereco >> n_bits_offset) & ((int)Math.pow(2, n_bits_indice) - 1);
				acessos++;
				
				if(assoc == 1) {
					//Mapeamento Direto
					if (cache_val[indice] == 0){
						miss_compulsorio++;
						miss++;
						cache_val[indice] = 1;		// tratamento falta
						cache_tag[indice] = tag;	// tratamento falta
					}
					else {
						if (cache_tag[indice] == tag) {
							hit++;
						}
						else{
							miss++;
							miss_conflito++;
							cache_val[indice] = 1;
							cache_tag[indice] = tag;
						} 
					}
				}
				else {
					if(nsets == 1) {
						// Mapeamento Totalmente Associativo
						
						boolean flag_hit = false, flag_compulsorio = false;
						
						for(int i=0; i<assoc && flag_hit == false && flag_compulsorio == false; i++) {
							
							if (cache_val[i] == 0) {
								flag_compulsorio = true;
								miss_compulsorio++;
								miss++;
								cache_val[i] = 1;
								cache_tag[i] = tag;
								lru_priority.add(i);
							}
							else {
								if (cache_tag[i] == tag) {
									flag_hit = true;
									hit++;
									int v = i;
									lru_priority.removeIf(n -> (n == v));
									lru_priority.add(i);
									
								}
								else { /* repetir o laço */ }
							}
						}
						
						if (flag_hit == false && flag_compulsorio == false) {
							miss_capacidade++;
							miss++;
							
							switch(subst) {
							case "R": //random
								value = aleatorio.nextInt(assoc);
								cache_val[value] = 1;
								cache_tag[value] = tag;
								
							case "F": //fifo
								cache_val[value] = 1;
								cache_tag[value] = tag;
								value++;
								if(value >= assoc) {
									value = 0;
								}
								break;
								
							case "L": //lru
								value = lru_priority.remove(0);
								lru_priority.add(value);
								cache_val[value] = 1;
								cache_tag[value] = tag;
								break;
								
							}
							
						}
					}
					else {
						// Mapeamento conjunto-associativo
						boolean flag_hit = false, flag_compulsorio = false;
						int tamanho_cache = assoc * nsets;
						int value_conjunto[] = new int[nsets];
						
						
						for(int i=(indice*assoc); i<(indice*assoc)+assoc && flag_hit == false && flag_compulsorio == false; i++) {
							
							if (cache_val[i] == 0) {
								flag_compulsorio = true;
								miss_compulsorio++;
								miss++;
								cache_val[i] = 1;
								cache_tag[i] = tag;
								lru_priority_conj[indice].add(i);
							}
							else if (cache_tag[i] == tag) {
								flag_hit = true;
								hit++;
								int v = i;
								lru_priority_conj[indice].removeIf(n -> (n == v));
								lru_priority_conj[indice].add(i);
							}
							else { /* repetir o laço */ }
						}
						
						if (flag_hit == false && flag_compulsorio == false) {
							
							if (miss_compulsorio == tamanho_cache) {
								miss_capacidade++;
								miss++;
							}
							else {
								miss_conflito++;
								miss++;
							}
							
							switch(subst) {
							case "R": //random
								value = aleatorio.nextInt(assoc);
								cache_val[(indice*assoc)+value] = 1;
								cache_tag[(indice*assoc)+value] = tag;
								break;
								
							case "F": //fifo
								cache_val[(indice*assoc)+value_conjunto[indice]] = 1;
								cache_tag[(indice*assoc)+value_conjunto[indice]] = tag;
								value_conjunto[indice]++;
								if(value_conjunto[indice] >= assoc) {
									value_conjunto[indice] = 0;
								}
								break;
								
							case "L": //lru
								value_conjunto[indice] = lru_priority_conj[indice].remove(0);
								lru_priority_conj[indice].add(value_conjunto[indice]);
								cache_val[value_conjunto[indice]] = 1;
								cache_tag[value_conjunto[indice]] = tag;
								break;
								
							}
						}
					}
				
				}
			}
			
			double taxa_hit = (double) hit / acessos;
			double taxa_miss = (double) miss / acessos;
			double taxa_miss_compulsorio = (double) miss_compulsorio / miss;
			double taxa_miss_capacidade = (double) miss_capacidade / miss;
			double taxa_miss_conflito = (double) miss_conflito / miss;

			if (flagOut == 0) {
				System.out.println("\noffset: " + n_bits_offset);
				System.out.println("indice: " + n_bits_indice);
				System.out.println("tag: " + n_bits_tag);
				
		       	System.out.printf("\nnsets = %d\n", nsets);
		       	System.out.printf("bsize = %d\n", bsize);
		       	System.out.printf("assoc = %d\n", assoc);
		       	System.out.printf("subst = %s\n", subst);
		       	System.out.printf("flagOut = %d\n", flagOut);
		       	System.out.printf("arquivo = %s\n", arquivoEntrada);
				
				System.out.println("\nAcessos: " + acessos);
				System.out.println("Hits: " + hit);
				System.out.println("Misses: " + miss);
				System.out.println("Compulsórios: " + miss_compulsorio);
				System.out.println("Conflito: " + miss_conflito);
				System.out.println("Capacidade: " + miss_capacidade);
				
				System.out.printf("\nTaxa hits: %.2f", taxa_hit);
				System.out.printf("\nTaxa misses: %.2f", taxa_miss);
				System.out.printf("\nTaxa misses compulsórios: %.2f", taxa_miss_compulsorio);
				System.out.printf("\nTaxa misses capacidade: %.2f", taxa_miss_capacidade);
				System.out.printf("\nTaxa misses conflito: %.2f", taxa_miss_conflito);

			}
			else {
				System.out.printf("%d %.2f %.2f %.2f %.2f %.2f", acessos, taxa_hit, taxa_miss,
						taxa_miss_compulsorio, taxa_miss_capacidade, taxa_miss_conflito);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
