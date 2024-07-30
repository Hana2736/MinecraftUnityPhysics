using UnityEngine;

public class PhysicsBlock : MonoBehaviour
{
    public int myId = -1;
    public Matrix4x4 myState;
    public Transform myChild;

    public void Start()
    {
        myState = Matrix4x4.identity;
        //bottom corner position to match Minecraft block_display
        myChild = transform.Find("BaseLoc");
    }

    public void Update()
    {
        //take the transform components and swap the Z and X to match Minecraft
        Vector3 pos = myChild.position;
        (pos.x, pos.z) = (pos.z, pos.x);
        Quaternion rot = myChild.rotation;
        (rot.x, rot.z) = (rot.z, rot.x);
        Vector3 scale = transform.localScale;
        (scale.x, scale.z) = (scale.z, scale.x);
        //create a transform matrix that Minecraft can read from modified data
        myState = Matrix4x4.TRS(pos, rot, scale);;
    }
}