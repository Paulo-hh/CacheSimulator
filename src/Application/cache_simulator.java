package Application;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Scanner;

public class cache_simulator {

	public static void main(String[] args) {
		
		Scanner sc = new Scanner(System.in);
		
		int nsets = sc.nextInt();
        int bsize = sc.nextInt();
        int assoc = sc.nextInt();
        char subst = sc.next().charAt(0);
        int flagOut = sc.nextInt();
        String arquivoEntrada = sc.next();
		
		sc.close();

        int miss_conpulsorio = 0;
        int hit = 0;
        int miss = 0;
        int acessos = 0;
        int miss_conflito = 0;
        int miss_capacidade = 0;

/*
        System.out.printf("nsets = %d\n", nsets);
        System.out.printf("bsize = %d\n", bsize);
        System.out.printf("assoc = %d\n", assoc);
        System.out.printf("subst = %s\n", subst);
        System.out.printf("flagOut = %d\n", flagOut);
        System.out.printf("arquivo = %s\n", arquivoEntrada);*/

        
        int[] cache_val = new int[nsets * assoc];
		int[] cache_tag = new int[nsets * assoc];
		
		int n_bits_offset = (int)(Math.log(bsize)/Math.log(2));
		System.out.println("offset: " + n_bits_offset);
		int n_bits_indice = (int)(Math.log(nsets)/Math.log(2));
		System.out.println("indice: " + n_bits_indice);
		int n_bits_tag = 32 - n_bits_offset - n_bits_indice;
		System.out.println("tag: " + n_bits_tag);

		
		try (BufferedInputStream br = new BufferedInputStream(new FileInputStream(arquivoEntrada))) {
		
			int x = 0;
			int bits = 0;
			System.out.println("Imprimindo os endereços para verificar:");
			while((x = br.read()) != -1) {
				bits = x;
				for(int i=0; i<3; i++) {
					bits = bits << 8;
					x = br.read();
					bits = bits | x;
				}
				int endereco = bits;
				System.out.println(endereco);
				int tag = endereco >> (n_bits_offset + n_bits_indice);
				int indice = (endereco >> n_bits_offset) & ((int)Math.pow(2, n_bits_offset - 1));
				acessos++;
				//Mapeamento Direto
				if (cache_val[indice] == 0){
					miss_conpulsorio++;
					cache_val[indice] = 1;
					cache_tag[indice] = tag;
				// estas duas últimas instruções representam o tratamento da falta.
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
			if(flagOut == 1) {
				System.out.println();
				double taxa_hit = (double) hit / acessos;
				double taxa_miss = (double) miss / acessos;
				double taxa_miss_conpulsorio = (double) (miss_conpulsorio / acessos) * 100;
				double taxa_miss_capacidade = (double) (miss_capacidade / acessos) * 100;
				double taxa_miss_conflito = (double) (miss_conflito / acessos) * 100;
				System.out.printf("%d %.2f %.2f %.2f %.2f %.2f", acessos, taxa_hit, taxa_miss,
						taxa_miss_conpulsorio, taxa_miss_capacidade, taxa_miss_conflito);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
