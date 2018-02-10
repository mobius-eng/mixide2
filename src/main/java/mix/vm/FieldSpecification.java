package mix.vm;

/** This class encapsulates the concept of field specification,
 *  of the form (L:R).
 */
public class FieldSpecification
{
	private final int fspec;
	
	public FieldSpecification(int left, int right)
	{
		fspec = 8*left + right;
	}

	public FieldSpecification(int spec)
	{
		fspec = spec;
	}
	
	public int first()
	{
		return fspec/8;
	}
	
	public int last()
	{
		return fspec%8;
	}
}
