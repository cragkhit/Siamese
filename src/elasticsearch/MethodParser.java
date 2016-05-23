package elasticsearch;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

public class MethodParser {
	public ArrayList<String> parseMethods(String filePath) {
		ArrayList<String> methodList = new ArrayList<String>();
		try {
			/* Parse and extract method body */
			FileInputStream in = new FileInputStream(filePath);
			CompilationUnit cu;
			try {
				// parse the file
				cu = JavaParser.parse(in);
				List<TypeDeclaration> typeDeclarations = cu.getTypes();
				for (TypeDeclaration typeDec : typeDeclarations) {
					List<BodyDeclaration> members = typeDec.getMembers();
					if (members != null) {
						for (BodyDeclaration member : members) {
							// extract the constructors
							if (member instanceof ConstructorDeclaration) {
								ConstructorDeclaration constructor = (ConstructorDeclaration) member;
								String cons = constructor.getDeclarationAsString() + constructor.getBlock();
								methodList.add(cons);
							}
							// extract all the methods
							else if (member instanceof MethodDeclaration) {
								MethodDeclaration method = (MethodDeclaration) member;
								String mthd = method.getDeclarationAsString() + method.getBody().toString();
								methodList.add(mthd);
							}
						}
					}
				}
			} finally {
				in.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return methodList;
	}
}
